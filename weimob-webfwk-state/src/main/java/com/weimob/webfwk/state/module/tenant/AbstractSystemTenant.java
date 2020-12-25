package com.weimob.webfwk.state.module.tenant;

import com.weimob.webfwk.state.abs.AbstractStateFormBase;

public interface AbstractSystemTenant extends AbstractStateFormBase {
    
    public String getCode();
    
    public String getName();
    
    public String getCodeLibGroup();
    
    public String getCodeNamespace();
    
    public default boolean isEnabled() {
        return SystemTenantService.STATES.ENABLED.getCode().equals(getState());
    }
    
    public default boolean isDisabled() {
        return !isEnabled();
    }
}
