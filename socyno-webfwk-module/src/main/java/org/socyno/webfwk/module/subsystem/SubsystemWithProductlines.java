package org.socyno.webfwk.module.subsystem;

import java.util.List;

import org.socyno.webfwk.module.productline.FieldProductline.OptionProductline;

public interface SubsystemWithProductlines extends SubsystemAbstractForm {
    
    public List<OptionProductline> getProductlines();
    
    public void setProductlines(List<OptionProductline> productlines);
}
