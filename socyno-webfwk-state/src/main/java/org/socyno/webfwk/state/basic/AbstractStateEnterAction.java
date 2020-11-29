package org.socyno.webfwk.state.basic;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.exec.StateFormEmptyTargetStateException;

public abstract class AbstractStateEnterAction<S extends AbstractStateForm> extends AbstractStateAction<S, AbstractStateForm, Void> {
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
