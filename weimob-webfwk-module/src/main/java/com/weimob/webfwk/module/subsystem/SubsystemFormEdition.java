package com.weimob.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubsystemFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "代码", required = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
