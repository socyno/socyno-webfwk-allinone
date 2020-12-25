package com.weimob.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "可访问的业务系统清单查询")
public class SubsystemQueryAccessable extends SubsystemQueryAbstract {
    
    @Override
    protected String requiredAccessEventKey() {
        return SubsystemService.getInstance().getFormAccessEventKey();
    }
    
    public SubsystemQueryAccessable(String keyword, Integer limit, Long page) {
        super(keyword, limit, page);
    }
    
    public SubsystemQueryAccessable(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemQueryAccessable(String keyword) {
        super(keyword);
    }
    
    public SubsystemQueryAccessable() {
        super();
    }
}
