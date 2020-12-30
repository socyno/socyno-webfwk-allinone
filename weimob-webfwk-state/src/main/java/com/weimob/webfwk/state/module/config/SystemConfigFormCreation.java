package com.weimob.webfwk.state.module.config;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldStringAllowOrDenied;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemConfigFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "键", required = true)
    private String name;
    
    @Attributes(title = "值", required = true, type = FieldText.class)
    private String value;
    
    @Attributes(title = "外部访问", required = true, type = FieldStringAllowOrDenied.class)
    private String external;
    
    @Attributes(title = "备注", type = FieldText.class)
    private String comment;
    
}
