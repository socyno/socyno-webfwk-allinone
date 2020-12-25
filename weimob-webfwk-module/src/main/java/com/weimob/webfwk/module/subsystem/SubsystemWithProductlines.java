package com.weimob.webfwk.module.subsystem;

import java.util.List;

import com.weimob.webfwk.module.productline.FieldProductline.OptionProductline;

public interface SubsystemWithProductlines extends SubsystemAbstractForm {
    
    public List<OptionProductline> getProductlines();
    
    public void setProductlines(List<OptionProductline> productlines);
}
