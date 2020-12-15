package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "所有的业务系统清单查询")
public class SubsystemQueryAll extends SubsystemQueryAbstract {
    
    protected String requiredAccessEventKey() {
        return null;
    }
    
    public SubsystemQueryAll(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemQueryAll(String keyword) {
        super(keyword);
    }
    
    public SubsystemQueryAll() {
        super();
    }
}
