package com.weimob.webfwk.module.productline;

import com.weimob.webfwk.state.abs.AbstractStateFormBase;

public interface ProductlineFormAbstract extends AbstractStateFormBase {
    
    public String getCode();
    
    public String getName();
    
    public Long getOwnerId();
    
    public String getDescription();
}
