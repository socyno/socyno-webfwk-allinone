package org.socyno.webfwk.state.module.token;

import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.service.AbstractSessionInterceptor;

public class SessionContextInterceptor extends AbstractSessionInterceptor {
    
    public final static SessionContextInterceptor DEFAULT = new SessionContextInterceptor();
    
    public SessionContextInterceptor() {
        super();
    }
    
    public SessionContextInterceptor(String weakValidation) {
        super(weakValidation);
    }
    
    public SessionContextInterceptor(String weakValidation, int allowedExpiredMinites) {
        super(weakValidation, allowedExpiredMinites);
    }
    
    @Override
    protected AbstractUser getAbstractUser(String username) throws Exception {
            return SystemUserService.getInstance().getSimple(username);
    }
    
    @Override
    protected void checkUserAndTokenInvlid(AbstractUser user, String token) throws Exception {
        if (user == null || user.isDisabled() || UserTokenService.checkTokenDiscard(token)) {
            throw new MessageException("不存在用户、已禁用、或令牌已注销");
        }
    }
}
