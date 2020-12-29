package com.weimob.webfwk.gateway.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.weimob.webfwk.state.module.user.SystemUserService;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MissingUserException;

public class SessionContextInterceptor extends com.weimob.webfwk.state.module.token.SessionContextInterceptor {
    
    public SessionContextInterceptor() {
        super();
    }
    
    public SessionContextInterceptor(String weakValidation) {
        super(weakValidation);
    }
    
    public SessionContextInterceptor(String weakValidation, int allowedExpiredMinites) {
        super(weakValidation, allowedExpiredMinites);
    }
    
    /**
     * 网关层增加 weimob sso ticket 方式的认证，便于外部系统通过 ticket 直接访问
     */
    @Override
    protected String getTokenContent(HttpServletRequest request) throws Exception {
        String ssoTicket;
        if (StringUtils.isNotBlank(ssoTicket = request.getParameter("__weimob_sso_ticket__"))) {
            SystemUserService.getInstance().forceSuToUser(ssoTicket, request.getParameter("__weimob_sso_service__"));
            if (StringUtils.isNotBlank(SessionContext.getSsoTicketOrNull())) {
                return SessionContext.getToken();
            }
            throw new MissingUserException("无效的单点认证令牌，拒绝访问");
        }
        return super.getTokenContent(request);
    }
    
}
