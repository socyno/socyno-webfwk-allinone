package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "所有的业务系统清单查询")
public class SubsystemListAllQuery extends SubsystemListAccessorQuery {
    
    protected String requiredAccessEventKey() {
        return null;
    }
    
    public SubsystemListAllQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemListAllQuery(String keyword) {
        super(keyword);
    }
    
    public SubsystemListAllQuery() {
        super();
    }
}
