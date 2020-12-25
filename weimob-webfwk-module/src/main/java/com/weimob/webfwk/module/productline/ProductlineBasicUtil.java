package com.weimob.webfwk.module.productline;

import com.weimob.webfwk.util.exception.NamingConflictedException;
import com.weimob.webfwk.util.exception.NamingFormatInvalidException;
import com.weimob.webfwk.util.tool.StringUtils;

public class ProductlineBasicUtil {
    
    public static void ensuerNameFormatValid(String name) throws NamingFormatInvalidException {
        if (StringUtils.isNotBlank(name) && name.matches("^[a-zA-Z][a-zA-Z0-9\\-\\_]+[a-zA-Z0-9]$")) {
            return;
        }
        throw new NamingFormatInvalidException("产品线名称不符合规范，要求：只能包括数字、字母、横线（-）或下划线（_），且必须以数字开头，以数字或字母结尾");
    }
    
    public static void ensureCodeOrNameNotExists(String code, String name, Long thisId) throws Exception {
        
        String sql = String.format("SELECT COUNT(*) FROM %s s WHERE (s.code LIKE ? OR s.name LIKE ?) %s",
                ProductlineService.getInstance().getFormTable(),
                thisId == null ? "" : String.format("AND s.id != %s", thisId));
        if (ProductlineService.getInstance().getFormBaseDao().queryAsObject(Long.class, sql,
                new Object[] { code, name }) > 0) {
            throw new NamingConflictedException("产品线的名称或代码已经被占用");
        }
    }
}
