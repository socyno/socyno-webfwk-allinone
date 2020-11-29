package org.socyno.webfwk.state.basic;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.exec.StateFormEmptyTargetStateException;

public abstract class AbstractStateSubmitAction<S extends AbstractStateForm, F extends AbstractStateForm> extends AbstractStateAction<S, F, Long> {
    
    @Override
    public EventFormType getEventFormType() throws Exception {
        return EventFormType.CREATE;
    }
    
    public AbstractStateSubmitAction(String display, String targetState) {
        super(display, (String)null, targetState);
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
