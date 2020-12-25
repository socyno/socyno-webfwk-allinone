package com.weimob.webfwk.modutil.authoriy;

import com.weimob.webfwk.state.authority.AuthorityScopeIdParser;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.tool.StringUtils;

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
