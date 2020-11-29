package org.socyno.webfwk.util.websocket;

import org.socyno.webfwk.util.remote.R;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

public abstract class AbstractWebSocketViewHandler extends BaseWebSocketHandler implements WebSocketConfigurer {
    
    private final AbstractWebSocketViewHandler instance;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(instance, getRequestPath()).setAllowedOrigins("*");
    }
    
    public AbstractWebSocketViewHandler() {
        instance = this;
    }
    
    public abstract String getRequestPath();
    
    public abstract WebSocketViewDefinition getFormViewDefinition(WebSocketRequest request, WebSocketSession session) throws Exception;
    
    @Override
    protected void preHandle(WebSocketSession session, WebSocketRequest request) throws Exception {
        writeResponse(session, R.ok().setData(getFormViewDefinition(request, session)));
    }
}
