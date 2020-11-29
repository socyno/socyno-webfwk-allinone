package org.socyno.webfwk.module.subsystem;

import java.util.List;

import org.socyno.webfwk.module.department.FieldDepartment.OptionProductline;

public interface SubsystemWithDepartments extends SubsystemAbstractForm {
    
    public List<OptionProductline> getProductlines();
    
    public void setProductlines(List<OptionProductline> productlines);
}
