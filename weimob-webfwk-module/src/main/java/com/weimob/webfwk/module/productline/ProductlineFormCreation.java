package com.weimob.webfwk.module.productline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "代码", required = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private Long owner;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
