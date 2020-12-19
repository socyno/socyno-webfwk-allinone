package org.socyno.webfwk.module.productline;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;

public interface ProductlineFormAbstract extends AbstractStateFormBase {
    
    public String getCode();
    
    public String getName();
    
    public Long getOwnerId();
    
    public String getDescription();
}
