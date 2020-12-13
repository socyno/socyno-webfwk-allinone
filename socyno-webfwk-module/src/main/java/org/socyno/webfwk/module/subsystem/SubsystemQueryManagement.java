package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "可管理的业务系统清单查询")
public class SubsystemQueryManagement extends SubsystemQueryAccessable {
    
    @Override
    protected String requiredAccessEventKey() {
        return SubsystemService.getInstance().getFormEventKey(SubsystemService.EVENTS.Update.getName());
    }
    
    public SubsystemQueryManagement(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemQueryManagement(String keyword) {
        super(keyword);
    }
    
    public SubsystemQueryManagement() {
        super();
    }
}
