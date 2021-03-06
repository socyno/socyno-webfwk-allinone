package org.socyno.webfwk.module.productline;

import java.util.List;

import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

public interface ProductlineWithSubsystems {
    
    public List<SubsystemFormSimple> getSubsystems();
    
    public void setSubsystems(List<SubsystemFormSimple> subsystems);
}
