package org.socyno.webfwk.module.application;

import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsCodeLevel;
import org.socyno.webfwk.module.release.build.FieldBuildService;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationFormEdition extends StateFormBasicInput implements ApplicationFormAbstract {
    
    @Attributes(title = "名称", readonly = true)
    private String name;
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "源码仓库类型", readonly = true)
    private String vcsType;
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "业务系统编号", readonly = true)
    private SubsystemFormSimple subsystem;
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "发布分支", readonly = true)
    private String releaseBranch;
    
    @Attributes(title = "类型", readonly = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "构建服务", required = true, type = FieldBuildService.class)
    private String buildService;
    
    @Attributes(title = "质量分级", required = true, type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "描述", required = true, type = FieldText.class)
    private String description;
}
