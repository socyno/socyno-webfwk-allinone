package com.weimob.webfwk.gateway.websocket;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.weimob.webfwk.gateway.websocket.WebSocketRedirector;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketRedirector {
    private final static int DEFAULT_TIMEOUT_MS = 30000;

    private final static Map<String, WebSocketClient> REMOTE_WEBSOCKET_CONNS = new ConcurrentHashMap<>();
    
    private void removeRemoteWsClient(final WebSocketSession session) {
        WebSocketClient wsClient;
        if (session != null && (wsClient = REMOTE_WEBSOCKET_CONNS.remove(session.getId())) != null) {
            try {
                wsClient.close();
            } catch (Exception ex) {
                
            } finally {
                try {
                    session.close();
                } catch (Exception ex) {
                    
                }
            }
        }
    }
    
    private WebSocketClient getRemoteWsClient(@NonNull final WebSocketSession session, final URI targetUri, final int connectTimeoutMs) throws Exception {
        WebSocketClient wsClient;
        int limited = CommonUtil.parseInteger(ContextUtil.getConfigTrimed("system.gateway.websocket.connections.limit"), 1024);
        if (REMOTE_WEBSOCKET_CONNS.size() > limited) {
            session.sendMessage(new TextMessage(CommonUtil.toJson(R.error("系统繁忙，请稍候再试。。。"))));
        }
        if ((wsClient = REMOTE_WEBSOCKET_CONNS.get(session.getId())) == null || !wsClient.isOpen()) {
            synchronized (session) {
                if ((wsClient = REMOTE_WEBSOCKET_CONNS.get(session.getId())) == null || !wsClient.isOpen()) {
                    Map<String, String> headers = new HashMap<>();
                    HttpHeaders headerx = session.getHandshakeHeaders();
                    for (String key : headerx.keySet()) {
                        if (log.isDebugEnabled() || ContextUtil.inDebugMode()) {
                            log.info("Websocket redirect request header : {} = {}", key, headerx.getFirst(key));
                        }
                        if (StringUtils.equalsIgnoreCase(key, "sec-websocket-extensions")) {
                            continue;
                        }
                        headers.put(key, headerx.getFirst(key));
                    }
                    wsClient = new WebSocketClient(targetUri, headers) {
                        @Override
                        public void onClose(int arg0, String arg1, boolean arg2) {
                            removeRemoteWsClient(session);
                            log.info("Server closed, close client sessoin: {}, {}, {}", arg0, arg1, arg2);
                        }
                        
                        @Override
                        public void onError(Exception ex) {
                            removeRemoteWsClient(session);
                            log.info("Server error, close client sessoin.", ex);
                            log.error(ex.toString(), ex);
                        }
                        
                        @Override
                        public void onMessage(String message) {
                            try {
                                if (log.isDebugEnabled() || ContextUtil.inDebugMode()) {
                                    log.info("WebSocket message recieved: {}", message);
                                }
                                session.sendMessage(new TextMessage(message));
                            } catch (Exception e) {
                                log.error("Send message to clint failed, close server sessoin", e);
                                removeRemoteWsClient(session);
                            }
                        }
                        
                        @Override
                        public void onOpen(ServerHandshake handshakedata) {
                            log.info("Connection opend : " + getURI().toString());
                        }
                    };
                    int timeoutMsUsed = connectTimeoutMs;
                    if (connectTimeoutMs <= 0) {
                        timeoutMsUsed = DEFAULT_TIMEOUT_MS;
                    }
                    if (!wsClient.connectBlocking(timeoutMsUsed, TimeUnit.MILLISECONDS)) {
                        try { wsClient.close(); } catch (Exception e) { }
                        log.error("WebSocket redirect connect failure : url = {}, session = {}", targetUri, session);
                        throw new WebSocketConnectFailureException(String.format("Websocket connect timeout : %s",
                                                targetUri));
                    }
                    REMOTE_WEBSOCKET_CONNS.put(session.getId(), wsClient);
                    /* Close  connection lost timer : disabled ping and pong time out check */
                    wsClient.setConnectionLostTimeout(-1);
                    log.info("WebSocket redirect connect success : url = {}, session = {}", targetUri, session);
                }
            }
        }
        return wsClient;
    }
    
    public WebSocketRedirector(@NonNull URI targetUri, @NonNull final TextMessage message,
                                @NonNull final WebSocketSession session) throws Exception {
        WebSocketClient wsClient = getRemoteWsClient(session, targetUri, 0);
        log.info("WebSocket message redirect : url = {}, message = {}, session = {}",
                targetUri, message, session);
        wsClient.send(message.getPayload());
    }
    
    public WebSocketRedirector(@NonNull URI targetUri, @NonNull final TextMessage message,
                                @NonNull final WebSocketSession session, Integer connectTimeoutMs) throws Exception {
        WebSocketClient wsClient = getRemoteWsClient(session, targetUri, connectTimeoutMs);
        log.info("WebSocket message redirect : url = {}, message = {}, session = {}",
                targetUri, message, session);
        wsClient.send(message.getPayload());
    }
}
