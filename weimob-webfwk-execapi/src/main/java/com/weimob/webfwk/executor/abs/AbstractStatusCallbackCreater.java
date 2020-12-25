package com.weimob.webfwk.executor.abs;

public interface AbstractStatusCallbackCreater {
    
    public void fetched(AbstractJobStatus status);
    
    public void completed(AbstractJobStatus status, Throwable exception);
    
}
