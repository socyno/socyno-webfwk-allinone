package com.weimob.webfwk.state.module.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemAuths;
import com.weimob.webfwk.state.field.OptionSystemAuth;
import com.weimob.webfwk.state.module.feature.SystemFeatureFormSimple.FieldOptionsState;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统功能")
public class SystemFeatureFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
