package org.socyno.webfwk.state.authority;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

@Slf4j
public class AuthoriyScopeIdParserFromApplication implements AuthorityScopeIdParser, AuthorityScopeIdMultipleParser {
    @Override
    public Long getAuthorityScopeId(Object applicationId) {
        if (applicationId == null || StringUtils.isBlank((String) (applicationId = applicationId.toString()))) {
            return null;
        }
        try {
            return SystemTenantDataSource.getMain().queryAsObject(Long.class,
                    "SELECT subsystem_id FROM application WHERE id = ? OR name = ? LIMIT 1",
                    new Object[] { applicationId, applicationId });
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }
    
    @Override
    public long[] getAuthorityScopeIds(Object applicationIds) {
        if (applicationIds == null) {
            return null;
        }
        long[] primitiveAppIds = null;
        if (applicationIds.getClass().isArray()) {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray((Object[])applicationIds);
        } else if (Collection.class.isAssignableFrom(applicationIds.getClass())) {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(((Collection<?>)applicationIds).toArray());
        } else {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(applicationIds);
        }
        if (primitiveAppIds == null || primitiveAppIds.length <= 0) {
            return null;
        }
        try {
            List<Long> subsystemIds = SystemTenantDataSource.getMain().queryAsList(Long.class,
                    String.format("SELECT subsystem_id FROM application WHERE id in (%s)",
                            StringUtils.join(primitiveAppIds, ',')));
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(subsystemIds.toArray());
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }
}
