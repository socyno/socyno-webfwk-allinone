package com.weimob.webfwk.state.abs;

import com.weimob.webfwk.state.util.StateFormBasicInput;

public abstract class AbstractStateCommentAction<S extends AbstractStateFormBase> extends AbstractStateAction<S, StateFormBasicInput, Void> {
    
    public static final String getFormLogEvent() {
        return "system:comment";
    }
    
    @Override
    public final Boolean messageRequired() {
        return true;
    }
    
    @Override
    public boolean getStateRevisionChangeIgnored() {
        return true;
    }
    
    public AbstractStateCommentAction(String display, String ...sourceStates) {
        super(display, sourceStates, (String)null);
    }
}
