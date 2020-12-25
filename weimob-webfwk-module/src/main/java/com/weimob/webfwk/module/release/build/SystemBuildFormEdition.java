package com.weimob.webfwk.module.release.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
@Attributes(title = "构建服务编辑")
public class SystemBuildFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "名称", readonly = true)
    private String code;
    
    @Attributes(title = "类型", required = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "标题", required = true)
    private String title;
    
    @Attributes(title = "描述", required = true, type = FieldText.class)
    private String description;
}
