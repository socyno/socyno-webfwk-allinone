package org.socyno.webfwk.module.department;

import org.socyno.webfwk.state.field.OptionSystemUser;

public interface DepartmentWithOwner {
    
    public OptionSystemUser getOwner();
    
    public void setOwner(OptionSystemUser owner);
}
