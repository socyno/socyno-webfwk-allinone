package org.socyno.webfwk.util.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.SimpleLock;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.reflect.TypeToken;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSimpleLockService {
    
    private final static int DEFAULT_TIMEOUT_SECONDS = 7200;
    
    protected abstract AbstractDao getDao();
    
    /**
     * 创建任务锁
     */
    public SimpleLock getLock(@NonNull String objectType, @NonNull Long objectId, String title) throws Exception {
        return getLock(objectType, objectId.toString(), title, null);
    }
    
    /**
     * 创建任务锁
     */
    public SimpleLock getLock(@NonNull String objectType, @NonNull String objectId, String title) throws Exception {
        return getLock(objectType, objectId, title, null);
    }
    
    /**
     * 创建任务锁
     */
    private SimpleLock getLock(@NonNull String objectType, @NonNull String objectId, String title, Integer timeoutSeconds) throws Exception {
        log.info("任务锁参数 = objectType = {}, objectId={}, title={}, createdBy=",
                                    objectType, objectId, title, SessionContext.getTokenDisplay());
        if (StringUtils.isAnyBlank(objectType, objectId.toString())) {
            throw new MessageException("任务锁唯一标识符(ObjectType && ObjectId)不可置空");
        }
        if (queryLock(objectType, objectId) != null) {
            throw new MessageException("任务锁已被占用，相同的任务正在执行中。");
        }
        try {
            final AtomicLong id = new AtomicLong();
            getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                "system_common_lock", new ObjectMap()
                    .put("object_type", objectType)
                    .put("object_id", objectId)
                    .put("title", title)
                    .put("locked", 1)
                    .put("#created_at", "NOW()")
                    .put("state", SimpleLock.STATES.created.name())
                    .put("created_user_id",SessionContext.getTokenUserId())
                    .put("created_user_name", SessionContext.getTokenDisplay())
                    .put("timeout_seconds", CommonUtil.ifNull(timeoutSeconds, DEFAULT_TIMEOUT_SECONDS))
                ), new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        r.next();
                        id.set(r.getLong(1));
                    }
            });
            return getLock(id.get());
        } catch(Exception e) {
            throw new MessageException("系统异常，无法创建任务任务锁。", e);
        }
    }
    
    /**
     * 根据锁任务类型和任务主键查询未释放的锁
     */
    public SimpleLock queryLock(@NonNull final String objectType, @NonNull final Object objectId) {
        try {
            SimpleLock lock =  getDao().queryAsObject(SimpleLock.class,
                "SELECT * from system_common_lock WHERE object_type = ? and object_id = ? and locked IS NOT NULL",
                new Object[] { StringUtils.trimToEmpty(objectType), StringUtils.trimToEmpty(objectId.toString()) }
            );
            if (lock != null && lock.alreadyTimeout()) {
                release(lock.getId(), null);
                lock = null;
            }
            return lock;
        } catch (Exception e) {
            throw new MessageException("系统异常，无法查询任务锁", e);
        }
    }
    
    public boolean releaseQuietly(long lockId, Boolean result) {
        try {
            release(lockId, result);
            return true;
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return false;
    }
    
    public void release(long lockId, Boolean result) throws Exception {
        log.info("释放任务锁 ： lock = {}, result = {}", lockId, result);
        /* 释放锁时, 将 locked 字段置 null 即可 */
        ObjectMap query = new ObjectMap()
                        .put("locked", null)
                        .put("#unlocked_at", "NOW()")
                        .put("unlocked_user_id", SessionContext.getTokenUserId())
                        .put("unlocked_user_name", SessionContext.getTokenDisplay())
                        .put("result", result)
                        .put("state", SimpleLock.STATES.released.name())
                        .put("=id", lockId);
        getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery("system_common_lock", query));
    }
    
    /**
     * 设置锁任务的日志文件地址
     * 
     * @param taskId
     * @param logfile
     */
    public void setLogFile(long lockId, String logfile) throws Exception {
        getDao().executeUpdate(
            "UPDATE system_common_lock SET logfile = ? WHERE id = ?",
            new Object[] { logfile, lockId }
        );
    }
    
    /**
     * 记录任务结果数据
     * 
     */
    public void setResultData(long lockId, Map<String, String> data) throws Exception {
        if (data == null || data.isEmpty()) {
            getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_common_lock_data", new ObjectMap()
                    .put("=task_id", lockId)
            ));
            return;
        }
        getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
            "system_common_lock_data", new ObjectMap()
                .put("=task_id", lockId)
                .put("result_data", CommonUtil.toJson(data))
        ));
    }
    

    /**
     * 获取任务结果数据
     * 
     */
    public Map<String, String> getResultData(long lockId) throws Exception {
        String resultData = getDao().queryAsObject(String.class, 
            "SELECT result_data FROM system_common_lock_data WHERE task_id = ?", 
            new Object[] {lockId});
        if (StringUtils.isBlank(resultData)) {
            return Collections.emptyMap();
        }
        
        return CommonUtil.fromJson(resultData ,new TypeToken<Map<String, String>>() {}.getType());
    }
    
    /**
     * 设置任务执行中
     * 
     * @param taskId
     */
    public void markRunning(long lockId) throws Exception {
        getDao().executeUpdate(
                "UPDATE system_common_lock SET state = ?, running_at = NOW() WHERE id = ?",
                new Object[] { SimpleLock.STATES.started.name(), lockId }
            );
    }
    
    
    public SimpleLock getLock(long lockId) throws Exception {
        return getDao().queryAsObject(SimpleLock.class,
            "SELECT * from system_common_lock WHERE id = ?",
            new Object[] { lockId }
        );
        
    }
    
    /**
     * 获取所任务的日志文件句柄
     * @return
     */
    public InputStream getLogsInputStream(long lockId) throws Exception {
        SimpleLock lock;
        if ((lock = getLock(lockId)) == null || StringUtils.isBlank(lock.getLogfile())) {
            return null;
        }
        return new FileInputStream(new File(lock.getLogfile()));
    }
    
    /**
     * 非线程类操作加解锁调用，需实现CommonLockExecutor的 excutor方法
     * 
     * @param objectType
     *            锁类型
     * @param objectId
     *            锁对象
     * @param title
     *            锁名称
     * @param excutor
     *            抽象方法，调用时需重写
     * @throws Exception
     * 
     *             执行如出异常，默认抛出，如需处理请重写exceptionHandler方法
     * 
     *             新增finally处理方法，如需处理请重写finallyHandler
     * 
     */
    public void lockAndRun(@NonNull String objectType, @NonNull Long objectId,
            String title, @NonNull CommonLockExecutor executor) throws Exception {
        lockAndRun(objectType, objectId.toString(), title, executor);
    }
    
    public void lockAndRun(@NonNull String objectType, @NonNull String objectId,
            String title, @NonNull CommonLockExecutor executor) throws Exception {
        SimpleLock lock = null;
        lock = getLock(objectType, objectId, title);
        boolean success = false;
        Throwable exception = null;
        try {
            executor.execute();
            success = true;
        } catch (Throwable e) {
            success = false;
            exception = e;
            log.error(e.toString(), e);
        } finally {
            releaseQuietly(lock.getId(), success);
            try {
                executor.onFinished(lock.getId(), exception);
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }
    
    public static abstract class CommonLockExecutor {
        
        public abstract void execute() throws Exception;
        
        public void onFinished(long lockId, Throwable exception) {
            
        }
    }
}
