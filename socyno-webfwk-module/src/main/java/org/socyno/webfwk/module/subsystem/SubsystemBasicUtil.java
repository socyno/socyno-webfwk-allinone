package org.socyno.webfwk.module.subsystem;

import org.socyno.webfwk.util.exception.NamingConflictedException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.tool.StringUtils;

public class SubsystemBasicUtil {
    
    public static void ensuerNameFormatValid(String name) throws NamingFormatInvalidException {
        if (StringUtils.isNotBlank(name) && name.matches("^[a-zA-Z][a-zA-Z0-9\\-\\_]+[a-zA-Z0-9]$")) {
            return;
        }
        throw new NamingFormatInvalidException("业务系统名称不符合规范，要求：只能包括数字、字母、横线（-）或下划线（_），且必须以数字开头，以数字或字母结尾");
    }
    
    public static void ensureCodeOrNameNotExists(String code, String name, Long thisId) throws Exception {
        
        String sql = String.format("SELECT COUNT(*) FROM %s s WHERE (s.code LIKE ? OR s.name LIKE ?) %s", 
                SubsystemService.getInstance().getFormTable(), thisId == null ? "" : String.format("AND s.id != %s", thisId));
        if (SubsystemService.getInstance().getFormBaseDao().queryAsObject(Long.class, sql, new Object[] { code, name }) > 0) {
            throw new NamingConflictedException("业务系统的名称或代码已经被占用");
        }
    }
}
