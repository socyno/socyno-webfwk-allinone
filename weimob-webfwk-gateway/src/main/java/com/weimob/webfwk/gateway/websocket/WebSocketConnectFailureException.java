package com.weimob.webfwk.gateway.websocket;

import com.weimob.webfwk.util.exception.MessageException;

public class WebSocketConnectFailureException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    public WebSocketConnectFailureException() {
        super();
    }
    
    public WebSocketConnectFailureException(String message) {
        super(message);
    }
    
}
