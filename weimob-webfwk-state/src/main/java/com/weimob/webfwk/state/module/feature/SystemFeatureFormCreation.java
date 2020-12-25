package com.weimob.webfwk.state.module.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemAuths;
import com.weimob.webfwk.state.field.OptionSystemAuth;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加系统功能")
public class SystemFeatureFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "代码", required = true, position = 1010)
    private String code;
    
    @Attributes(title = "名称", required = true, position = 1020)
    private String name;
    
    @Attributes(title = "描述", position = 1030, type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", position = 1040, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
