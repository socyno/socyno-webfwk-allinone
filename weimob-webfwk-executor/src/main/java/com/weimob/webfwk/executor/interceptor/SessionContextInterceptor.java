package com.weimob.webfwk.executor.interceptor;

import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.model.AbstractUser;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.service.AbstractSessionInterceptor;

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
