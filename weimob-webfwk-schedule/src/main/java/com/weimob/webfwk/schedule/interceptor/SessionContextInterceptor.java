package com.weimob.webfwk.schedule.interceptor;

import com.weimob.webfwk.util.model.AbstractUser;
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
    
}
