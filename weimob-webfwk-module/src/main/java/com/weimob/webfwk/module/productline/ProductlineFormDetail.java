package com.weimob.webfwk.module.productline;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.subsystem.FieldSubsystemAccessable;
import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormDetail extends ProductlineFormDefault implements ProductlineWithSubsystems {
    
    @Attributes(title = "业务系统清单", type = FieldSubsystemAccessable.class)
    private List<SubsystemFormSimple> subsystems;
    
}
