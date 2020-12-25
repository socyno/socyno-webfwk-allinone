package com.weimob.webfwk.module.application;

import com.weimob.webfwk.util.exception.NamingFormatInvalidException;
import com.weimob.webfwk.util.tool.StringUtils;

public class ApplicationBasicUtil {
    
    public static void ensureNameFormatValid(String name) throws NamingFormatInvalidException {
        if (checkNameFormatValid(name)) {
            return;
        }
        throw new NamingFormatInvalidException("应用名称不符合规范，要求只能包括数字、小写字母、横线（-）、下划线（_）或圆点（.），且必须以数字开头，以数字或字母结尾");
    }
    
    public static boolean checkNameFormatValid(String name) throws NamingFormatInvalidException {
        return StringUtils.isNotBlank(name) && name.matches("^[a-z][a-z0-9\\-\\_\\.]+[a-z0-9]$");
    }
}
