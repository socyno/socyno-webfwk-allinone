package org.socyno.webfwk.state.module.tenant;

import org.socyno.webfwk.state.abs.AbstractStateFormInput;

public interface AbstractSystemTenantInput extends AbstractStateFormInput {
    public String getCode();
    
    public String getName();
    
    public String getCodeLibGroup();
    
    public String getCodeNamespace();
}
