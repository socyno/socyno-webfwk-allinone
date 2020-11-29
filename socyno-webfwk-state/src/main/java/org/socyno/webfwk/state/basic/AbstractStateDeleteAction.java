package org.socyno.webfwk.state.basic;

public abstract class AbstractStateDeleteAction<S extends AbstractStateForm> extends AbstractStateAction<S, BasicStateForm, Void> {
    
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
