package com.weimob.webfwk.state.abs;

public interface AbstractStateAsyncView {
    public boolean waitingForFinished(String event, String message, AbstractStateFormBase originForm, AbstractStateFormInput form)
                throws Exception;
}
