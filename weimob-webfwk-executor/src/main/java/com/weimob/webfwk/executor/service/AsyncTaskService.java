package com.weimob.webfwk.executor.service;

import com.weimob.webfwk.executor.model.JobBasicStatus;
import com.weimob.webfwk.executor.model.JobStatusEnum;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.model.SimpleLock;
import com.weimob.webfwk.util.service.AbstractAsyncTaskService;
import com.weimob.webfwk.util.sql.AbstractDao;

public class AsyncTaskService extends AbstractAsyncTaskService {
    
    @Override
    protected AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    public static final AsyncTaskService DEFAULT = new AsyncTaskService();
    
    
    public static <T extends JobBasicStatus> T toJobBasicStatus(Class<T> clazz, SimpleLock task) {
        
        T status = null; 
        try {
            status = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        status.setTaskId(task.getId());
        status.setCreatedAt(task.getCreatedAt());
        status.setStartedAt(task.getRunningAt());
        status.setCompletedAt(task.getUnlockedAt());
        status.setCreatedBy(task.getCreatedCodeBy());
        status.setStatus(task.isFinished() ? (task.getResult() ? JobStatusEnum.SUCCESS : JobStatusEnum.FAILURE)
                : (task.isRunning() ? JobStatusEnum.RUNNING
                        : (task.isPending() ? JobStatusEnum.PENDING : JobStatusEnum.UNKNOWN)));
        return status;
    }
}
