package org.socyno.webfwk.gateway.websocket;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.socyno.webfwk.gateway.util.HttpRedirectUtil;
import org.socyno.webfwk.gateway.util.HttpRedirectUtil.ServiceBackend;
import org.socyno.webfwk.util.exception.PageNotFoundException;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketRedirectorToBackend extends TextWebSocketHandler {
    
    private Pattern REGEXP_HTTP_PREFIX = Pattern.compile("^(http)s?:", Pattern.CASE_INSENSITIVE);
    public final static TextWebSocketHandler DEFAUTL = new WebSocketRedirectorToBackend();
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String path;
        String targetPath = null;
        if ((path = session.getUri().toString()) != null) {
            String wsPath = path.replaceAll("^.*?/ws/+", "");
            String wsBackend = String.format("/%s/", StringUtils.substringBefore(wsPath, "/"));
            ServiceBackend backend;
            if ((backend = ServiceBackend.getWebSocket(wsBackend)) != null) {
                targetPath = HttpUtil.concatUrlPath(backend.getRequestRootUrl(), StringUtils.substringAfter(wsPath, "/"));
            }
        }
        if (StringUtils.isBlank(targetPath)) {
            session.close();
            throw new PageNotFoundException();
        }
        Matcher matcher;
        if ((matcher = REGEXP_HTTP_PREFIX.matcher(targetPath)) != null && matcher.find()) {
            targetPath = String.format("ws%s", targetPath.substring(matcher.group(1).length()));
        }
        new WebSocketRedirector(new URI(targetPath), message, session, HttpRedirectUtil.getRequestTimeouMs());
    }
}
