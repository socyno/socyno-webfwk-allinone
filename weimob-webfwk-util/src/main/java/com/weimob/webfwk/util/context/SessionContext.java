package com.weimob.webfwk.util.context;

import com.weimob.webfwk.util.exception.TenantMissingException;
import com.weimob.webfwk.util.model.AbstractUser;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

public class SessionContext {
    
    private final static ThreadLocal<UserContext> USERCONTEXT = new ThreadLocal<>();
    
    public static void setUserContext(UserContext userContext) {
        AbstractUser user;
        if (userContext != null && (user = userContext.getSysUser()) != null) {
            userContext.setTenant(user.getTenant());
            userContext.setTokenUserId(user.getId());
            userContext.setTokenDisplay(user.getDisplay());
            userContext.setTokenUsername(user.getUsername());
        }
        USERCONTEXT.set(userContext);
    }
    
    public static UserContext getUserContext() {
        return USERCONTEXT.get();
    }
    
    private static AbstractUser getSysUserOrNull() {
        AbstractUser user;
        UserContext context;
        if ((context = USERCONTEXT.get()) != null && (user = context.getSysUser()) != null) {
            return user;
        }
        return null;
    }
    
    public static boolean hasUserSession() {
        return hasTokenSession();
    }
    
    public static boolean hasTokenSession() {
        return !StringUtils.isBlank(getToken());
    }
    
    public static Long getUserId() {
        return getTokenUserId();
    }
    
    public static String getUsername() {
        return getTokenUsername();
    }
    
    public static String getDisplay() {
        return getTokenDisplay();
    }
    
    public static boolean isAdmin() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return false;
        }
        return CommonUtil.ifNull(context.isAdmin(), false);
    }
    
    public static String getToken() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getToken();
    }
    
    public static String getTokenHead() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getTokenHead();
    }
    
    public static Long getTokenUserId() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getTokenUserId();
    }
    
    public static String getTokenDisplay() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getTokenDisplay();
    }
    
    public static String getTokenUsername() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getTokenUsername();
    }
    
    public static String getProxyUsername() {

        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getProxyUsername();
    }
    
    public static String getProxyDisplay() {

        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getProxyDisplay();
    }
    
    public static String getTenant() {
        String tenant;
        if (StringUtils.isNotBlank(tenant = getTenantOrNull())) {
            return tenant;
        }
        throw new TenantMissingException();
    }

    public static String getSsoTicketOrNull() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        return context.getSsoTicket();
    }
    
    public static String getTenantOrNull() {
        UserContext context;
        if ((context = USERCONTEXT.get()) == null) {
            return null;
        }
        AbstractUser user;
        if ((user = getSysUserOrNull()) != null) {
            return user.getTenant();
        }
        return context.getTenant();
    }
}
