package org.socyno.webfwk.executor.websocket;

import org.socyno.webfwk.executor.service.ContextBackendService;
import org.socyno.webfwk.util.websocket.WebSocketViewDefinition;

public class RemoteWebSocketViewDefinition extends WebSocketViewDefinition {
    
    public RemoteWebSocketViewDefinition(Class<?> formClass) throws Exception {
        this(formClass, false);
    }
    
    public RemoteWebSocketViewDefinition(Class<?> formClass, boolean singleResponse) throws Exception {
        super(ContextBackendService.getInstance().parseFormClassSchema(formClass), singleResponse);
    }
}
