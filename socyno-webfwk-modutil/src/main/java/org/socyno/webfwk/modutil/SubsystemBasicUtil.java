package org.socyno.webfwk.modutil;

import java.util.Collection;

import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NamingConflictedException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class SubsystemBasicUtil {
    
    private final static String SUBSYSTEM_BUSINESS_PREFIX = "app-";
    
    public static String getFormTableName() {
        return "subsystem";
    }

    public static AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    public static void ensuerNameFormatValid(String name) throws NamingFormatInvalidException {
        if (StringUtils.isNotBlank(name) && name.matches("^[a-zA-Z][a-zA-Z0-9\\-\\_]+[a-zA-Z0-9]$")) {
            return;
        }
        throw new NamingFormatInvalidException("业务系统名称不符合规范，要求：只能包括数字、字母、横线（-）或下划线（_），且必须以数字开头，以数字或字母结尾");
    }
    
    public static void ensureCodeOrNameNotExists(String code, String name, Long thisId) throws Exception {
        
        String sql = String.format("SELECT COUNT(*) FROM %s s WHERE (s.code LIKE ? OR s.name LIKE ?) %s", 
                getFormTableName(), thisId == null ? "" : String.format("AND s.id != %s", thisId));
        if (getFormBaseDao().queryAsObject(Long.class, sql, new Object[] { code, name }) > 0) {
            throw new NamingConflictedException("业务系统的名称或代码已经被占用");
        }
    }
    
    public static String subsytemIdToBusinessId(long subsystemId) {
        return String.format("%s%s", SUBSYSTEM_BUSINESS_PREFIX, subsystemId);
    }
    
    public static String[] subsytemIdToBusinessId(long[] subsystemIds) {
        if (subsystemIds == null) {
            return null;
        }
        String[] businessIds = new String[subsystemIds.length];
        for (int i = 0; i < subsystemIds.length; i++) {
            businessIds[i] = subsytemIdToBusinessId(subsystemIds[i]);
        }
        return businessIds;
    }
    
    public static String[] subsytemIdToBusinessId(Long[] subsystemIds) {
        if (subsystemIds == null) {
            return null;
        }
        return subsytemIdToBusinessId(ConvertUtil.asNonNullUniquePrimitiveLongArray(subsystemIds));
    }
    
    public static String[] subsytemIdToBusinessId(Collection<Long> subsystemIds) {
        if (subsystemIds == null) {
            return null;
        }
        return subsytemIdToBusinessId(ConvertUtil.asNonNullUniquePrimitiveLongArray(subsystemIds));
    }
    
    public static Long subsytemIdFromBusinessId(String businessId, boolean skipNotMatched) {
        String subsystemId;
        if (StringUtils.isBlank(businessId) 
                || !businessId.startsWith(SUBSYSTEM_BUSINESS_PREFIX)
                || !(subsystemId = businessId.substring(SUBSYSTEM_BUSINESS_PREFIX.length())).matches("^\\d+$")) {
            if (skipNotMatched) {
                return null;
            }
            throw new MessageException(String.format("Invalid subsystem business id : %s", businessId));
        }
        return Long.valueOf(subsystemId);
    }
    
    public static long subsytemIdFromBusinessId(String businessId) {
        return subsytemIdFromBusinessId(businessId, false);
    }
    
    public static long[] subsytemIdFromBusinessId(String[] businessIds, boolean skipNotMatched) {
        if (businessIds == null) {
            return null;
        }
        Long[] subsystemIds = new Long[businessIds.length];
        for (int i = 0; i < businessIds.length; i++) {
            subsystemIds[i] = subsytemIdFromBusinessId(businessIds[i], skipNotMatched);
        }
        return ConvertUtil.asNonNullUniquePrimitiveLongArray(subsystemIds);
    }
    
    public static long[] subsytemIdFromBusinessId(Collection<String> businessIds, boolean skipNotMatched) {
        if (businessIds == null) {
            return null;
        }
        return subsytemIdFromBusinessId(businessIds.toArray(new String[0]), skipNotMatched);
    }
    
    public static long[] subsytemIdFromBusinessId(String[] businessIds) {
        return subsytemIdFromBusinessId(businessIds, false);
    }
    
    public static long[] subsytemIdFromBusinessId(Collection<String> businessIds) {
        return subsytemIdFromBusinessId(businessIds, false);
    }
    
}
