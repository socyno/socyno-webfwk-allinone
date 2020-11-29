package org.socyno.webfwk.module.systenant;

import org.socyno.webfwk.state.basic.AbstractStateForm;

public interface AbstractSystemTenant extends AbstractStateForm {
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
