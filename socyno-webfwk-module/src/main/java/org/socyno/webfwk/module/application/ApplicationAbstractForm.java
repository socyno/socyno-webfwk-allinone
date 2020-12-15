package org.socyno.webfwk.module.application;

import org.socyno.webfwk.module.systenant.AbstractSystemTenant;
import org.socyno.webfwk.module.systenant.SystemTenantService;
import org.socyno.webfwk.module.vcs.common.VcsType;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.SchemaIgnore;

public interface ApplicationAbstractForm extends AbstractStateForm {
    
    public String getName();
    
    public String getType();
    
    public String getReleaseBranch();
    
    public Long getSubsystemId();
    
    public String getVcsType();
    
    @SchemaIgnore
    public default String getCodeRepoNamedId() throws Exception {
        AbstractSystemTenant tenant;
        if ((tenant = SystemTenantService.getInstance().getSimple(SessionContext.getTenant())) == null
                || StringUtils.isBlank(tenant.getCodeNamespace())) {
            throw new MessageException("获取租户代码空间未设置");
        }
        return String.format("%s/%s", tenant.getCodeNamespace(), getName());
    }
    
    @SchemaIgnore
    public default VcsType getVcsTypeEnum() {
        return VcsType.forName(getVcsType());
    }

    @SchemaIgnore
    public default boolean isPatchReleaseMode() {
        return StringUtils.startsWith(getReleaseBranch(), getVcsTypeEnum().getPatchesPrefix());
    }

    @SchemaIgnore
    public default boolean isMasterReleaseMode() {
        return StringUtils.equals(getReleaseBranch(), getVcsTypeEnum().getMasterName());
    }
}
