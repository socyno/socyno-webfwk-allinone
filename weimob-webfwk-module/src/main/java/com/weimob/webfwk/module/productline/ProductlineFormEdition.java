package com.weimob.webfwk.module.productline;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.subsystem.FieldSubsystemNoAnyLimited;
import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private OptionSystemUser owner;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "业务系统", type = FieldSubsystemNoAnyLimited.class)
    private List<SubsystemFormSimple> subsystems;
}
