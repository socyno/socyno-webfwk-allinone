package org.socyno.webfwk.state.abs;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.socyno.webfwk.state.exec.StateFormChoiceTooDeepException;
import org.socyno.webfwk.state.exec.StateFormTargetChoiceException;
import org.socyno.webfwk.state.util.StateFormSimpleChoice;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

@Data
public abstract class AbstractStateChoice {

    private final String display;
    private final String targetState;
    private final AbstractStateChoice trueState;
    private final AbstractStateChoice falseState;
    
    
    @Getter
    @Setter
    @Accessors(chain = true)
    private static class EventContext {
        
        private String event;
        
        private AbstractStateFormBase originForm;
        
        private AbstractStateFormService<?> service;
        
        EventContext(AbstractStateFormService<?> service, String event, AbstractStateFormBase originForm) {
            this.event = event;
            this.service = service;
            this.originForm = originForm;
        }
    }
    
    private final static ThreadLocal<EventContext> CONTEXT_FORM_SERVICE = new ThreadLocal<>();
    
    private void setEventContext(AbstractStateFormService<?> service, String event, AbstractStateFormBase originForm) {
        if (CONTEXT_FORM_SERVICE.get() == null) {
            CONTEXT_FORM_SERVICE.set(new EventContext(service, event, originForm));
            return;
        }
        CONTEXT_FORM_SERVICE.get().setService(service)
                                .setEvent(event)
                                .setOriginForm(originForm);
    }
    
    private void clearEventContext() {
        setEventContext(null, null, null);
    }
    
    protected String getContextFormEvent() {
        return CONTEXT_FORM_SERVICE.get().getEvent();
    }
    
    protected AbstractStateFormBase getContextFormOrigin() {
        return CONTEXT_FORM_SERVICE.get().getOriginForm();
    }
    
    protected AbstractStateFormService<?> getContextFormService() {
        return CONTEXT_FORM_SERVICE.get().getService();
    }
    
    protected AbstractStateChoice(String targetState) {
        this(null, targetState, null ,null);
    }
    
    protected AbstractStateChoice(AbstractStateChoice targetState) {
        this(null, targetState.getTargetState(), null ,null);
    }
    
    public AbstractStateChoice(String display, String trueState, String falseState) {
        this(display, null, StateFormSimpleChoice.getInstance(trueState), StateFormSimpleChoice.getInstance(falseState));
    }
    
    public AbstractStateChoice(String display, String trueState, AbstractStateChoice falseState) {
        this(display, null, StateFormSimpleChoice.getInstance(trueState), falseState);
    }
    
    public AbstractStateChoice(String display, AbstractStateChoice trueState, String falseState) {
        this(display, null, trueState, StateFormSimpleChoice.getInstance(falseState));
    }
    
    public AbstractStateChoice(String display, AbstractStateChoice trueState, AbstractStateChoice falseState) {
        this(display, null, trueState, falseState);
    }
    
    private AbstractStateChoice(String display, String targetState, AbstractStateChoice trueState, AbstractStateChoice falseState) {
        boolean simpleIsBlank = StringUtils.isBlank(targetState);
        if ((simpleIsBlank && trueState == null) || (!simpleIsBlank && (trueState != null || falseState != null))) {
            throw new StateFormTargetChoiceException();
        }
        this.trueState = trueState;
        this.falseState = falseState;
        this.targetState = targetState;
        this.display = CommonUtil.ifNull(display, targetState);
    }
    
    public boolean isSimple() {
        return (this instanceof StateFormSimpleChoice)
                || (getTrueState() == null && getFalseState() == null);
    }
    
    public String getTargetState(@NonNull AbstractStateFormInput form, AbstractStateFormBase originForm,
                        @NonNull AbstractStateFormService<?> formService, @NonNull String event) {
        int maxDeepth = 15;
    	AbstractStateChoice currentChoice = this;
        AbstractStateChoice nextTargetChoice = null;
        while (currentChoice != null && !currentChoice.isSimple()) {
        	if (maxDeepth-- <= 0) {
                clearEventContext();
        		throw new StateFormChoiceTooDeepException(this);
        	}
        	try {
                currentChoice.setEventContext(formService, event, originForm);
        	    nextTargetChoice = currentChoice.select(form) ? currentChoice.getTrueState() 
        	                                : currentChoice.getFalseState();
        	} catch(Exception e) {
        	    if (e instanceof RuntimeException) {
        	        throw (RuntimeException)e;
        	    }
        	    throw new RuntimeException(e);
        	} finally {
        	    currentChoice.clearEventContext();
        	    currentChoice = nextTargetChoice;
        	}
        }
        if (currentChoice == null) {
            return null;
        }
        return currentChoice.getTargetState();
    }
    
    public boolean flowMatched(AbstractStateFormBase form, boolean yesNo) {
        return true;
    }
    
    protected abstract boolean select(AbstractStateFormInput form) throws Exception;
}
