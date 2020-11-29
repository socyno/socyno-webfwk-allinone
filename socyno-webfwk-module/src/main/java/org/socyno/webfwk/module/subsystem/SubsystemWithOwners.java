package org.socyno.webfwk.module.subsystem;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemUser;

public interface SubsystemWithOwners extends SubsystemAbstractForm {
    
    public List<OptionSystemUser> getOwners();
    
    public void setOwners(List<OptionSystemUser> owners);
}
