package org.socyno.webfwk.modutil.authoriy;

import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.tool.StringUtils;

import lombok.SneakyThrows;

public class AuthoriyScopeIdParserFromSubsystemCode implements AuthorityScopeIdParser {
    @Override
    @SneakyThrows
    public String getAuthorityScopeId(Object subsystemCode) {
        if (subsystemCode == null || StringUtils.isBlank((String) (subsystemCode = subsystemCode.toString()))) {
            return null;
        }
        return SystemTenantDataSource.getMain().queryAsObject(String.class,
                "SELECT CONCAT('app-', id) FROM subsystem WHERE code = ? LIMIT 1",
                new Object[] { subsystemCode });
    }
}
