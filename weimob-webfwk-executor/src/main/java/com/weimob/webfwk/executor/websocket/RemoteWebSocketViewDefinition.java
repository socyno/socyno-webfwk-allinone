package com.weimob.webfwk.executor.websocket;

import com.weimob.webfwk.executor.service.ContextBackendService;
import com.weimob.webfwk.util.websocket.WebSocketViewDefinition;

public class RemoteWebSocketViewDefinition extends WebSocketViewDefinition {
    
    public RemoteWebSocketViewDefinition(Class<?> formClass) throws Exception {
        this(formClass, false);
    }
    
    public RemoteWebSocketViewDefinition(Class<?> formClass, boolean singleResponse) throws Exception {
        super(ContextBackendService.getInstance().parseFormClassSchema(formClass), singleResponse);
    }
}
