package com.weimob.webfwk.executor.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import com.weimob.webfwk.executor.interceptor.SessionContextInterceptor;
import com.weimob.webfwk.util.context.SpringContextUtil;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.websocket.AbstractWebSocketViewHandler;

@Configuration
@EnableWebSocket
public abstract class WebSocketHanlderWithTokenInterceptor extends AbstractWebSocketViewHandler {
    
    @Override
    protected void preHandle(WebSocketSession session, WebSocketRequest request) throws Exception {
        SpringContextUtil.getBean(SessionContextInterceptor.class).preHandle(session);
        writeResponse(session, R.ok().setData(getFormViewDefinition(request, session)));
    }
    
}
