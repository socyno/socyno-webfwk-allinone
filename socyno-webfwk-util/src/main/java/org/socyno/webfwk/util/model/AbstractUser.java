package org.socyno.webfwk.util.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.exception.TenantMissingException;
import org.socyno.webfwk.util.tool.StringUtils;


public interface AbstractUser {
    
    public static final String ENABLED = "enabled";
    static final Pattern RegexpTenant = Pattern.compile(".+@([^@\\s]+)$");
    
    public Long getId();
    public String getState();
    public String getUsername();
    public String getDisplay();
    public String getMailAddress();
    
    public default boolean isEnabled() {
        return ENABLED.equalsIgnoreCase(getState());
    }
    
    public default boolean isDisabled() {
        return !isEnabled();
    }
    
    public default String getTenant() {
        return parseTenantFromUsername(getUsername());
    }
    
    public static String parseTenantFromUsername(String username) {
        Matcher matcher;
        if (StringUtils.isNotBlank(username) && (matcher = RegexpTenant.matcher(username)) != null
                && matcher.find()) {
            return matcher.group(1);
        }
        throw new TenantMissingException();
    }
    
    public static void ensuerNameFormatValid(String name) throws NamingFormatInvalidException {
        if (StringUtils.isNotBlank(name) && name.matches("^[a-z][a-z0-9\\-\\_\\.]+[a-z0-9]$")) {
            return;
        }
        throw new NamingFormatInvalidException("用户的账户名称不规范：只能包含数字、小写字母、下滑线（_）、短横线（-）或圆点（.），且必须以字母开头、以数字或字母结尾");
    }
    
}
