package org.socyno.webfwk.modutil.authoriy;

import lombok.SneakyThrows;

import java.util.Collection;

import org.socyno.webfwk.modutil.SubsystemBasicUtil;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class AuthoriyScopeIdParserFromApplication implements AuthorityScopeIdParser, AuthorityScopeIdMultipleParser {
    @Override
    @SneakyThrows
    public String getAuthorityScopeId(Object applicationIdOrName) {
        if (applicationIdOrName == null
                || StringUtils.isBlank((String) (applicationIdOrName = applicationIdOrName.toString()))) {
            return null;
        }
        if (applicationIdOrName.toString().matches("^\\d+$")) {
            return SubsystemBasicUtil.subsytemIdToBusinessId(
                    SystemTenantDataSource.getMain().queryAsObject(Long.class,
                        "SELECT subsystem_id FROM application WHERE id = ?",
                        new Object[] { applicationIdOrName }));
        }
        return SubsystemBasicUtil.subsytemIdToBusinessId(
                SystemTenantDataSource.getMain().queryAsObject(Long.class,
                    "SELECT subsystem_id FROM application WHERE name = ?",
                    new Object[] { applicationIdOrName }));
    }
    
    @Override
    @SneakyThrows
    public String[] getAuthorityScopeIds(Object applicationIds) {
        if (applicationIds == null) {
            return null;
        }
        long[] primitiveAppIds = null;
        if (applicationIds.getClass().isArray()) {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray((Object[]) applicationIds);
        } else if (Collection.class.isAssignableFrom(applicationIds.getClass())) {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(((Collection<?>) applicationIds).toArray());
        } else {
            primitiveAppIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(applicationIds);
        }
        if (primitiveAppIds == null || primitiveAppIds.length <= 0) {
            return null;
        }
        return SubsystemBasicUtil.subsytemIdToBusinessId(
                SystemTenantDataSource.getMain().queryAsList(Long.class,
                    String.format("SELECT subsystem_id FROM application WHERE id in (%s)",
                            StringUtils.join(primitiveAppIds, ','))));
    }
}
