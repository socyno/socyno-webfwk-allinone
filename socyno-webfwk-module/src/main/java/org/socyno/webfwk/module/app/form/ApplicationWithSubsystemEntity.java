package org.socyno.webfwk.module.app.form;

import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;

public interface ApplicationWithSubsystemEntity {
    public SubsystemBasicForm getSubsystem();
    public void setSubsystem(SubsystemBasicForm subsystem);
}
