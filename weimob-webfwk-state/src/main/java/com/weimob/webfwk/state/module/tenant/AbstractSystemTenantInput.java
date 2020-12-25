package com.weimob.webfwk.state.module.tenant;

import com.weimob.webfwk.state.abs.AbstractStateFormInput;

public interface AbstractSystemTenantInput extends AbstractStateFormInput {
    public String getCode();
    
    public String getName();
    
    public String getCodeLibGroup();
    
    public String getCodeNamespace();
}
