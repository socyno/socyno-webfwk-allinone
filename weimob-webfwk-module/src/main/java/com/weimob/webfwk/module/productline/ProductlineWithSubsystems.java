package com.weimob.webfwk.module.productline;

import java.util.List;

import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;

public interface ProductlineWithSubsystems {
    
    public List<SubsystemFormSimple> getSubsystems();
    
    public void setSubsystems(List<SubsystemFormSimple> subsystems);
}
