package com.weimob.webfwk.state.module.role;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemFeatureWithTenant;
import com.weimob.webfwk.state.field.OptionSystemFeature;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统角色")
public class SystemRoleFormEdition extends StateFormBasicInput implements SystemRoleSaved  {
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "描述")
    private String description;

    @Attributes(title = "授权的功能", type = FieldSystemFeatureWithTenant.class)
    private List<OptionSystemFeature> features;
}
