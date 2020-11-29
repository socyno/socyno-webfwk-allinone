package org.socyno.webfwk.module.app.form;

import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsApplicationType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsCodeLevel;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsVcsType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsYesOrNo;
import org.socyno.webfwk.module.subsystem.FieldSubsystemManagement;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AccessLevel;

@Getter
@Setter
@ToString
@Attributes(title = "创建新的应用")
public class ApplicationFormForCreation implements ApplicationAbstractForm {
    
    @Attributes(title = "编号", readonly = true, position = -1)
    private Long id;
    
    @Attributes(title = "状态", readonly = true, position = -1)
    private String state;
    
    @Attributes(title = "版本", readonly = true, position = -1)
    private Long revision;
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "发布分支(接口需要，页面无需显示)", readonly = true, position = -1)
    private String releaseBranch;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "类型", required = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "源码仓库类型", required = true, type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "业务系统", required = true, type = FieldSubsystemManagement.class)
    private Long subsystemId;
    
    @Attributes(title = "质量分级", required = true, type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "是否有状态", required = true, type = FieldOptionsYesOrNo.class)
    private Integer stateless;
    
    @Attributes(title = "描述", required = true, type = FieldText.class)
    private String description;
}
