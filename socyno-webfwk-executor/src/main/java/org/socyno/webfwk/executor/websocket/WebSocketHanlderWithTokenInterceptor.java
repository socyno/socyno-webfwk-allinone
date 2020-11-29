package org.socyno.webfwk.executor.websocket;

import org.socyno.webfwk.executor.interceptor.SessionContextInterceptor;
import org.socyno.webfwk.util.context.SpringContextUtil;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.websocket.AbstractWebSocketViewHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@EnableWebSocket
public abstract class WebSocketHanlderWithTokenInterceptor extends AbstractWebSocketViewHandler {
    
    @Override
    protected void preHandle(WebSocketSession session, WebSocketRequest request) throws Exception {
        SpringContextUtil.getBean(SessionContextInterceptor.class).preHandle(session);
        writeResponse(session, R.ok().setData(getFormViewDefinition(request, session)));
    }
    
}
