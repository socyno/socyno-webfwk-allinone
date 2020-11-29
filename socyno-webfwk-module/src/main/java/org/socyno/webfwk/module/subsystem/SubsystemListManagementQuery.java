package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "可管理的业务系统清单查询")
public class SubsystemListManagementQuery extends SubsystemListAccessorQuery {
    
    @Override
    protected String requiredAccessEventKey() {
        return SubsystemService.DEFAULT.getFormEventKey(SubsystemService.EVENTS.Update.getName());
    }
    
    public SubsystemListManagementQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemListManagementQuery(String keyword) {
        super(keyword);
    }
    
    public SubsystemListManagementQuery() {
        super();
    }
}
