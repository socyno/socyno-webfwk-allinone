package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;

public interface DepartmentWithSubsystems {
    
    public List<SubsystemBasicForm> getSubsystems();
    
    public void setSubsystems(List<SubsystemBasicForm> subsystems);
}
