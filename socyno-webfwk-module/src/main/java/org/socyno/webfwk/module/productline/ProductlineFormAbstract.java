package org.socyno.webfwk.module.productline;

import org.socyno.webfwk.state.basic.AbstractStateForm;

public interface ProductlineFormAbstract extends AbstractStateForm {
    
    public String getCode();
    
    public String getName();
    
    public Long getOwnerId();
    
    public String getDescription();
}
