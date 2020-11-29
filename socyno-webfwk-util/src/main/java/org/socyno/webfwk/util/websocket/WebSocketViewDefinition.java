package org.socyno.webfwk.util.websocket;


import org.socyno.webfwk.util.tool.ClassUtil;

import lombok.Data;
import lombok.NonNull;

@Data
public class WebSocketViewDefinition {
    
    private final String formClass;
    private final boolean singleResponse;
    
    public WebSocketViewDefinition(Class<?> formClass) throws Exception {
        this(formClass, false);
    }
    
    public WebSocketViewDefinition(@NonNull Class<?> formClass, boolean singleResponse) throws Exception {
        this(ClassUtil.classToJson(formClass).toString(), singleResponse);
    }
    
    protected WebSocketViewDefinition(String formClass, boolean singleResponse) throws Exception {
        this.formClass = formClass;
        this.singleResponse = singleResponse;
    }
}
