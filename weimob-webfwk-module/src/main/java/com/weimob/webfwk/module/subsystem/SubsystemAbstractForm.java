package com.weimob.webfwk.module.subsystem;

import com.weimob.webfwk.state.abs.AbstractStateFormBase;

public interface SubsystemAbstractForm extends AbstractStateFormBase {
    
    public String getCode();
    
    public String getName();
    
    public String getDescription();
}
