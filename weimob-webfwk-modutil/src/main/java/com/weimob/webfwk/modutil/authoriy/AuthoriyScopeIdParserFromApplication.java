package com.weimob.webfwk.modutil.authoriy;

import lombok.SneakyThrows;

import java.util.Collection;

import com.weimob.webfwk.modutil.SubsystemBasicUtil;
import com.weimob.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import com.weimob.webfwk.state.authority.AuthorityScopeIdParser;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.tool.ConvertUtil;
import com.weimob.webfwk.util.tool.StringUtils;

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
