package org.socyno.webfwk.module.app.form;

import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.tool.StringUtils;

public class ApplicationBasicUtil {
    
    public static void ensuerNameFormatValid(String name) throws NamingFormatInvalidException {
        if (StringUtils.isNotBlank(name) && name.matches("^[a-z][a-z0-9\\-\\_\\.]+[a-z0-9]$")) {
            return;
        }
        throw new NamingFormatInvalidException("应用名称不符合规范，要求只能包括数字、小写字母、横线（-）、下划线（_）或圆点（.），且必须以数字开头，以数字或字母结尾");
    }
}
