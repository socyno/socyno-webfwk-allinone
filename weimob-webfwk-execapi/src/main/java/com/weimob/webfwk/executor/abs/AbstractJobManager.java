package com.weimob.webfwk.executor.abs;

import java.util.concurrent.atomic.AtomicInteger;

import com.weimob.webfwk.executor.model.JobStatusEnum;
import com.weimob.webfwk.executor.model.JobStatusWebsocketLink;
import com.weimob.webfwk.util.context.RunableWithSessionContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJobManager<P extends AbstractJobParameters, S extends AbstractJobStatus> {
    
    private final static int DefaultCheckInterval = 30;
    
    protected abstract String getTaskPath();
    
    protected abstract int getTimeoutSeconds();
    
    public static AbstractJobManager<?, ?> getInstance(String name) {
        return null;
    }
    
    protected  int getCheckIntervalSeconds() {
        return DefaultCheckInterval;
    }
    
    protected abstract AbstractJobExecutorService getService();
    
    public final int getFinalCheckIntervalSeconds() {
       int interval;
       if ((interval = getCheckIntervalSeconds()) < 1) { 
           interval = DefaultCheckInterval;
       }
       return interval;
    }
    
    /**
     * 获取参数的实际类型
     */
    @SuppressWarnings("unchecked")
    public Class<P> getParametersTypeClass() {
        return (Class<P>) ClassUtil.getActualParameterizedType(this.getClass(), AbstractJobManager.class, 0);
    }
    
    /**
     * 获取状态的实际类型
     */
    @SuppressWarnings("unchecked")
    public Class<S> getStatusTypeClass() {
        return (Class<S>)ClassUtil.getActualParameterizedType(this.getClass(), AbstractJobManager.class, 1);
    }
    
    /**
     * 触发远端任务, 内部调用。
     */
    @SuppressWarnings("unchecked")
    public long trigger(Object parameters, AbstractStatusCallbackCreater creater) throws Exception {
        if (parameters.getClass() != getParametersTypeClass()) {
            throw new IllegalArgumentException();
        }
        return trigger((P)parameters, createCallback(creater));
    }
    
    /**
     * 触发远端任务。
     */
    protected AbstractJobStatusCallback<S> createCallback(final AbstractStatusCallbackCreater creater) {
        if (creater == null) {
            return null;
        }
        return new AbstractJobStatusCallback<S>() {

            @Override
            public void fetched(S status) {
                creater.fetched(status);
                
            }

            @Override
            public void completed(S status, Throwable exception) {
                creater.completed(status, exception);
                
            }
        };
    }
    
    /**
     * 触发远端任务
     */
    public long trigger(@NonNull P parameters, AbstractJobStatusCallback<S> callback) throws Exception {
        AbstractJobExecutorService service = getService();
        ClassUtil.checkFormRequiredAndOpValue(parameters);
        ObjectMap headers = new ObjectMap();
        if (StringUtils.isNotBlank(getService().getTokenHead())
                && StringUtils.isNotBlank(service.getTokenData())) {
            headers.put(service.getTokenHead(), service.getTokenData());
        }
        final long taskId = service.getService().post(Long.class, HttpUtil.concatUrlPath(getTaskPath(), "start"),
                parameters, null, headers.asMap());
        if (callback != null) {
            new Thread(new RunableWithSessionContext() {
                @Override
                public void exec() {
                    S status = null;
                    Throwable exception = null;
                    final int checkInterval = getFinalCheckIntervalSeconds();
                    final int checkTimeoutSenconds = getTimeoutSeconds();
                    final AtomicInteger times = new AtomicInteger(0);
                    final AtomicInteger errors = new AtomicInteger(0);
                    while (true) {
                        try {
                            Thread.sleep(checkInterval * 1000L);
                            status = status(taskId);
                            errors.set(0);
                            if (JobStatusEnum.SUCCESS.equals(status.getStatus())
                                    || JobStatusEnum.FAILURE.equals(status.getStatus())) {
                                break;
                            }
                            callback.fetched(status);
                        } catch (Throwable e) {
                            log.error(e.toString(), e);
                            if (errors.incrementAndGet() >= 5) {
                                exception = new RuntimeException(e);
                                break;
                            }
                        }
                        if (checkTimeoutSenconds > 0 && times.getAndAdd(checkInterval) > checkTimeoutSenconds) {
                            exception = new MessageException("TIMEDOUT");
                        }
                    }
                    callback.completed(status, exception);
                }
            }).start();
        }
        return taskId;
    }
    
    /**
     * 获取远端任务状态
     */
    public S status(long taskId) throws Exception {
        ObjectMap headers = new ObjectMap();
        AbstractJobExecutorService service = getService();
        if (StringUtils.isNotBlank(service.getTokenHead())
                && StringUtils.isNotBlank(service.getTokenData())) {
            headers.put(service.getTokenHead(), service.getTokenData());
        }
        return service.getService().get(getStatusTypeClass(),
                HttpUtil.concatUrlPath(HttpUtil.concatUrlPath(getTaskPath(), "status"), "" + taskId),
                null, headers.asMap());
    }
    
    /**
     * 获取实时状态的 WebSocket 访问地址
     */
    public JobStatusWebsocketLink getStatusWebSocketLink(long taskId) {
        return new JobStatusWebsocketLink(taskId, getService().getRealtimeStatusUrl());
    }

    
}
