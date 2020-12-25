package com.weimob.webfwk.state.abs;

import org.apache.commons.lang3.StringUtils;

import com.weimob.webfwk.state.exec.StateFormEmptyTargetStateException;

public abstract class AbstractStateLeaveAction<S extends AbstractStateFormBase> extends AbstractStateAction<S, AbstractStateFormInput, Void> {
    public AbstractStateLeaveAction(String display, String sourceState) {
        super(display, (String)null, sourceState);
        if (StringUtils.isBlank(sourceState)) {
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
