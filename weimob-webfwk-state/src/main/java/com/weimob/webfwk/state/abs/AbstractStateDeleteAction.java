package com.weimob.webfwk.state.abs;

import com.weimob.webfwk.state.util.StateFormBasicInput;

public abstract class AbstractStateDeleteAction<S extends AbstractStateFormBase> extends AbstractStateAction<S, StateFormBasicInput, Void> {
    
    @Override
    public EventFormType getEventFormType() throws Exception {
        return EventFormType.DELETE;
    }
    
    @Override
    public final boolean confirmRequired() {
        return true;
    }
    
    @Override
    public final Boolean messageRequired() {
        return true;
    }
    
    @Override
    public boolean getStateRevisionChangeIgnored() throws Exception {
        return true;
    }
    
    public AbstractStateDeleteAction(String display, String ...sourceStates) {
        super(display, sourceStates, (String)null);
    }
}
