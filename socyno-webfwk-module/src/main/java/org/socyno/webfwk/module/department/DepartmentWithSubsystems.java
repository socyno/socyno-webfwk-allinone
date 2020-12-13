package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

public interface DepartmentWithSubsystems {
    
    public List<SubsystemFormSimple> getSubsystems();
    
    public void setSubsystems(List<SubsystemFormSimple> subsystems);
}
