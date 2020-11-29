package org.socyno.webfwk.module.department;

import org.socyno.webfwk.state.basic.AbstractStateForm;

public interface DepartmentAbstractForm extends AbstractStateForm {
    
    public String getCode();
    
    public String getName();
    
    public Long getOwnerId();
    
    public String getDescription();
}
