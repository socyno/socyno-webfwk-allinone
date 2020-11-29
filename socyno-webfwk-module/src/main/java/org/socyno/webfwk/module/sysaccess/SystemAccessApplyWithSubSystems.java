package org.socyno.webfwk.module.sysaccess;

import java.util.List;

public interface SystemAccessApplyWithSubSystems {
    public List<SystemAccessApplySubSystemEntity> getSubSystems();
    public void setSubSystems(List<SystemAccessApplySubSystemEntity>  subSystems);
}
