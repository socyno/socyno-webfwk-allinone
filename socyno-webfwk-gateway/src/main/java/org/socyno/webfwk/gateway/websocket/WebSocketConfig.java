package org.socyno.webfwk.gateway.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(WebSocketRedirectorToBackend.DEFAUTL, "/ws/**/*")
                .setAllowedOrigins("*");
        registry.addHandler(WebSocketRedirectorToBackend.DEFAUTL,"/sockjs/ws/**/*")
                .setAllowedOrigins("*")
                .withSockJS(); 
    }
}