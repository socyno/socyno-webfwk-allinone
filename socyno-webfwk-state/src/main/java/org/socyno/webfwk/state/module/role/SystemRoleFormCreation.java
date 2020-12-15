package org.socyno.webfwk.state.module.role;

import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.FieldSystemFeatureWithTenant;
import org.socyno.webfwk.state.field.OptionSystemFeature;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加系统角色")
public class SystemRoleFormCreation implements AbstractStateForm, SystemRoleSaved {
    @Attributes(title = "编号", readonly = true)
    private Long   id;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long   revision;
    
    @Attributes(title = "代码", position = 1010, required = true)
    private String code;
    
    @Attributes(title = "名称", position = 1020, required = true)
    private String name;
    
    @Attributes(title = "描述", position = 1030)
    private String description;
    
    @Attributes(title = "授权的功能", position = 1040, type = FieldSystemFeatureWithTenant.class)
    private List<OptionSystemFeature> features;
}
