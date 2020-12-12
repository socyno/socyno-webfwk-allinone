package org.socyno.webfwk.util.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.RunableWithSessionContext;
import org.socyno.webfwk.util.model.SimpleLock;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.CommonUtil;

@Slf4j
public abstract class AbstractAsyncTaskService {

    protected abstract AbstractDao getDao();

    private final AbstractSimpleLockService lockService;

    private final AbstractAsyncTaskService getTaskServiceInstance() {
        return this;
    }

    public AbstractAsyncTaskService() {
        lockService = new AbstractSimpleLockService() {
            @Override
            protected AbstractDao getDao() {
                return getTaskServiceInstance().getDao();
            }
        };
    }

    @Getter
    static class ContextTaskFuture {

        private final SimpleLock task;

        private final Future<?> future;

        ContextTaskFuture(@NonNull SimpleLock task, @NonNull Future<?> future) {
            this.task = task;
            this.future = future;
        }
    }

    private final static Map<Long, ContextTaskFuture> TASK_FUTURES = new ConcurrentHashMap<>();

    private static ThreadPoolExecutor TASK_EXECUTOR = null;

    private static ThreadPoolExecutor getTaskExecutor() {
        if (TASK_EXECUTOR == null) {
            synchronized (AbstractAsyncTaskService.class) {
                ThreadPoolExecutor temporary = TASK_EXECUTOR;
                if (temporary == null) {
                    synchronized (AbstractAsyncTaskService.class) {
                        int coreSize = CommonUtil.parseInteger(ContextUtil.getConfigTrimed(
                                "system.common.async.task.pool.core.size"), 5);
                        int maxSize = CommonUtil.parseInteger(ContextUtil.getConfigTrimed(
                                "system.common.async.task.pool.maximum.size"), 20);
                        int queueSize = CommonUtil.parseInteger(ContextUtil.getConfigTrimed(
                                "system.common.async.task.pool.queue.size"), 512);
                        temporary = new ThreadPoolExecutor(coreSize, maxSize, 10L, TimeUnit.MINUTES,
                                new ArrayBlockingQueue<Runnable>(queueSize), new AbortPolicy());
                    }
                    TASK_EXECUTOR = temporary;
                }
            }
        }
        return TASK_EXECUTOR;
    }

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                ContextTaskFuture future;
                for (Long taskId : TASK_FUTURES.keySet().toArray(new Long[0])) {
                    try {
                        /* 移除已经结束或超时的任务 */
                        future = TASK_FUTURES.get(taskId);
                        if (future == null || future.getTask().alreadyTimeout()) {
                            if (future != null && !future.getFuture().isDone()) {
                                future.getFuture().cancel(true);
                            }
                            TASK_FUTURES.remove(taskId);
                        }
                    } catch (Exception e) {
                        log.error("Failed to load configs.", e);
                    }
                }
            }
        }, 300, 300, TimeUnit.SECONDS);
    }

    @Getter
    @Setter
    public static abstract class AsyncTaskExecutor extends RunableWithSessionContext {

        private final String targetType;

        private final String targetId;

        private final String title;

        private Long taskId = null;

        private String logfile = null;

        private AbstractSimpleLockService lockService = null;

        @Setter(AccessLevel.NONE)
        private boolean finished = false;

        @Setter(AccessLevel.NONE)
        private Exception exception = null;

        private final Map<String, String> dataMap;

        public AsyncTaskExecutor(String targetType, String targetId, String title) {
            this.title = title;
            this.targetId = targetId;
            this.targetType = targetType;
            this.dataMap = new HashMap<>();
        }

        public abstract boolean execute(FileOutputStream logsOutputStream, String logsDir) throws Exception;

        protected void dataPut(String name, String value) {
            dataMap.put(name, value);
        }

        protected String dataRemove(String name) {
            return dataMap.remove(name);
        }

        protected String dataGet(String name) {
            return dataMap.get(name);
        }

        protected boolean dataContains(String name) {
            return dataMap.containsKey(name);
        }

        protected String[] dataKeys(String name) {
            return dataMap.keySet().toArray(new String[0]);
        }

        @Override
        public final void exec() {
            Boolean success = false;
            FileOutputStream logsOutputStream = null;
            try {
                File logsFile = new File(getLogfile());
                logsOutputStream = FileUtils.openOutputStream(logsFile);
                lockService.markRunning(taskId);
                success = execute(logsOutputStream, logsFile.getParent());
                lockService.setResultData(taskId, dataMap);
            } catch (Throwable e) {
                log.error(e.toString(), e);
                try {
                    if (logsOutputStream != null) {
                        IOUtils.write(CommonUtil.stringifyStackTrace(e), logsOutputStream, "UTF-8");
                    }
                } catch(Exception ex) {
                    log.error(ex.toString(), ex);
                }
            } finally {
                finished = true;
                TASK_FUTURES.remove(taskId);
                if (logsOutputStream != null) {
                    IOUtils.closeQuietly(logsOutputStream);
                }
                if (taskId != null) {
                    lockService.releaseQuietly(taskId, success);
                }
            }
        }
    }

    public SimpleLock getStatus(long taskId) throws Exception {
        return lockService.getLock(taskId);
    }

    public Map<String,String> getResultData(long taskId) throws Exception {
        return lockService.getResultData(taskId);
    }

    public SimpleLock waitingForFinished(long taskId, boolean abortIfTimeout) throws Exception {
        ContextTaskFuture future;
        SimpleLock task;
        while ((task = lockService.getLock(taskId)).isLocked()) {
            if (task.alreadyTimeout() && abortIfTimeout) {
                if ((future = TASK_FUTURES.remove(taskId)) != null && future.getFuture() != null
                        && !future.getFuture().isDone()) {
                    future.getFuture().cancel(true);
                }
                lockService.releaseQuietly(taskId, false);
            }
            Thread.sleep(500);
        }
        TASK_FUTURES.remove(taskId);
        return task;
    }

    public long execute(AsyncTaskExecutor executor) throws Exception {
        SimpleLock lock = lockService.getLock(executor.getTargetType(),
                executor.getTargetId(), executor.getTitle());
        long taskId = lock.getId();
        String logfile = String.format("%s/%s/async-%s/task.log",
                ContextUtil.getConfigTrimed("system.common.async.task.logs"),
                DateFormatUtils.format(new Date(), "yyyy/MM/dd"),
                taskId);
        lockService.setLogFile(taskId, logfile);
        executor.setTaskId(taskId);
        executor.setLogfile(logfile);
        executor.setLockService(lockService);
        try {
            ContextTaskFuture future = new ContextTaskFuture(lock, getTaskExecutor().submit(executor));
            TASK_FUTURES.put(taskId, future);
        } catch(Throwable ex) {
            FileUtils.write(new File(logfile), CommonUtil.stringifyStackTrace(ex), "UTF-8", true);
            lockService.releaseQuietly(taskId, false);
        }
        return taskId;
    }
}
