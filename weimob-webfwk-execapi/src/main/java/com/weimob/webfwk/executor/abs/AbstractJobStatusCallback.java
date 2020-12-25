package com.weimob.webfwk.executor.abs;

public interface AbstractJobStatusCallback <S extends AbstractJobStatus> {
    
    public void fetched(S status);
    
    public void completed(S status, Throwable exception);
    
}
