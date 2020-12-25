package com.weimob.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsCodeLevel;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsVcsType;
import com.weimob.webfwk.module.subsystem.FieldSubsystemManagement;
import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AccessLevel;

@Getter
@Setter
@ToString
@Attributes(title = "创建新的应用")
public class ApplicationFormCreation extends StateFormBasicInput implements ApplicationFormAbstract {
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "发布分支(接口需要，页面无需显示)", readonly = true)
    private String releaseBranch;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "类型", required = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "源码仓库类型", required = true, type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "业务系统", required = true, type = FieldSubsystemManagement.class)
    private SubsystemFormSimple subsystem;
    
    @Attributes(title = "质量分级", required = true, type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "描述", required = true, type = FieldText.class)
    private String description;
}
