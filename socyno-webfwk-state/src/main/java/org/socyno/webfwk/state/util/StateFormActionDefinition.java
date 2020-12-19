package org.socyno.webfwk.state.util;

import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.abs.AbstractStateAction.EventFormType;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.google.gson.annotations.Expose;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class StateFormActionDefinition {
    
    private StateFormActionDefinition() {
        
    }
    
    private String name;
    
    private String formName;
    
    private EventType eventType;
    
    private String display;
    
    private String targetState;
    
    private String[] sourceStates;
    
    @Expose(serialize = false, deserialize = false)
    private Authority authority;
    
    private boolean asyncEvent;
    
    private boolean dynamicEvent;
    
    private boolean visible = true;
      
    private Boolean messageRequired;
    
    private boolean confirmRequired;
    
    private boolean prepareRequired;
    
    private boolean stateRevisionChangeIgnored;
    
    private EventFormType eventFormType;
    
    private String formClass;
    
    private String resultClass;
    
    public static StateFormActionDefinition fromStateAction(String name, String formName, @NonNull AbstractStateAction<?, ?, ?> action)
        throws Exception {
        return  new StateFormActionDefinition()
                .setName(name)
                .setFormName(formName)
                .setDisplay(action.getDisplay())
                .setEventType(getEventType(action))
                .setEventFormType(action.getEventFormType())
                .setTargetState(action.getTargetStateForDisplay())
                .setSourceStates(action.getSourceStates())
                .setAsyncEvent(action.isAsyncEvent())
                .setPrepareRequired(action.prepareRequired())
                .setConfirmRequired(action.confirmRequired())
                .setMessageRequired(action.messageRequired())
                .setStateRevisionChangeIgnored(action.getStateRevisionChangeIgnored())
                .setFormClass(ClassUtil.classToJson(action.getFormTypeClass()).toString())
                .setResultClass(ClassUtil.classToJson(action.getReturnTypeClass()).toString())
                .setAuthority(action.getAuthority())
                .setDynamicEvent(action.isDynamicEvent())
                
                
                ;
    }

    @Getter
    public static enum EventType {
          Normal("normal")
        , Create("create")
        , Delete("delete")
        , Comment("comment")
        ;
        
        private final String name;
        EventType(String name) {
            this.name = name;
        }
        
        public static EventType fromKeyOrName(String name) {
            for (EventType e : EventType.values()) {
                if (e.name().equalsIgnoreCase(name) || e.getName().equalsIgnoreCase(name)) {
                    return e;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static EventType getEventType(@NonNull AbstractStateAction<?, ?, ?> action) {
        EventType eventType = EventType.Normal;
        if (action instanceof AbstractStateCommentAction) {
            eventType = EventType.Comment;
        } else if (action instanceof AbstractStateCreateAction) {
            eventType = EventType.Create;
        } else if (action instanceof AbstractStateDeleteAction) {
            eventType = EventType.Delete;
        }
        return eventType;
    }
}
