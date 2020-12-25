package com.weimob.webfwk.state.abs;

import org.apache.commons.lang3.StringUtils;

import com.weimob.webfwk.state.exec.StateFormEmptyTargetStateException;

public abstract class AbstractStateCreateAction<S extends AbstractStateFormBase, F extends AbstractStateFormInput>
        extends AbstractStateAction<S, F, AbstractStateCreateView> {
    
    @Override
    public EventFormType getEventFormType() throws Exception {
        return EventFormType.CREATE;
    }
    
    public AbstractStateCreateAction(String display, String targetState) {
        super(display, (String) null, targetState);
        if (StringUtils.isBlank(targetState)) {
            throw new StateFormEmptyTargetStateException();
        }
    }
    
    @Override
    public Boolean messageRequired() {
        return null;
    }
    
    @Override
    public boolean allowHandleReturnNull() {
        return false;
    }
}
