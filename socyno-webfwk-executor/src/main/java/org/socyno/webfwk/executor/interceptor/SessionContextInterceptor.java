package org.socyno.webfwk.executor.interceptor;

import java.util.Map;

import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.service.AbstractSessionInterceptor;
import org.springframework.web.socket.WebSocketSession;

public class SessionContextInterceptor extends AbstractSessionInterceptor {
    
    @Override
    protected AbstractUser getAbstractUser(String username) {
        return null;
    }

    @Override
    protected void checkUserAndTokenInvlid(AbstractUser user, String token) {
        return;
    }

    public SessionContextInterceptor(String weakValidation) {
        super(weakValidation);
    }

    public SessionContextInterceptor(String weakValidation, int allowedExpiredMinites) {
        super(weakValidation, allowedExpiredMinites);
    }
    
    public boolean preHandle(WebSocketSession session) throws Exception {
        String tokenHeader = getTokenHeader();
        Map<String, String[]> parameters = HttpUtil.parseQueryString(session.getUri().getQuery());
        String[] tokenContent = parameters.get("__" + tokenHeader);
        SessionContext.setUserContext(null);
        return tokenValidation(tokenHeader, tokenContent == null ? "" : tokenContent[0]);
    }
}
