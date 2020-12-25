package com.weimob.webfwk.module.application;

import com.github.reinert.jjschema.SchemaIgnore;
import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;
import com.weimob.webfwk.module.vcs.common.VcsType;
import com.weimob.webfwk.state.abs.AbstractStateFormInput;
import com.weimob.webfwk.state.module.tenant.AbstractSystemTenant;
import com.weimob.webfwk.state.module.tenant.SystemTenantService;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.StringUtils;

public interface ApplicationFormAbstract extends AbstractStateFormInput {
    
    public String getName();
    
    public String getType();
    
    public String getReleaseBranch();
    
    public SubsystemFormSimple getSubsystem();
    
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
