package com.weimob.webfwk.state.abs;

import org.apache.commons.lang3.StringUtils;

import com.weimob.webfwk.state.exec.StateFormEmptyTargetStateException;

public abstract class AbstractStateEnterAction<S extends AbstractStateFormBase> extends AbstractStateAction<S, AbstractStateFormInput, Void> {
    public AbstractStateEnterAction(String display, String targetState) {
        super(display, (String)null, targetState);
        if (StringUtils.isBlank(targetState)) {
            throw new StateFormEmptyTargetStateException();
        }
    }
    
    @Override
    public void check(String event, S originForm, String sourceState) {
        
    }
    
    protected boolean executeWhenNoStateChanged() {
        return false;
    }
}
