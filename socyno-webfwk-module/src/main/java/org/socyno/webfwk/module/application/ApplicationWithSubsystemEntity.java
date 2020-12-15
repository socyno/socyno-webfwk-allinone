package org.socyno.webfwk.module.application;

import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

public interface ApplicationWithSubsystemEntity {
    public SubsystemFormSimple getSubsystem();
    public void setSubsystem(SubsystemFormSimple subsystem);
}
