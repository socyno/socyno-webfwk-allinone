package org.socyno.webfwk.state.authority;

import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.tool.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthoriyScopeIdParserFromSubsystemCode implements AuthorityScopeIdParser {
    @Override
    public Long getAuthorityScopeId(Object subsystemCode) {
        if (subsystemCode == null || StringUtils.isBlank((String) (subsystemCode = subsystemCode.toString()))) {
            return null;
        }
        try {
            return SystemTenantDataSource.getMain().queryAsObject(Long.class,
                    "SELECT id FROM subsystem WHERE code = ? LIMIT 1", new Object[] { subsystemCode });
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }
}
