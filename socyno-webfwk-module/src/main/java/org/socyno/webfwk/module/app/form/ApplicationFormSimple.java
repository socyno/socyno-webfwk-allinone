package org.socyno.webfwk.module.app.form;

import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsApplicationType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsCodeLevel;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsState;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsVcsType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsYesOrNo;
import org.socyno.webfwk.module.release.build.FieldSysBuildService;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationFormSimple implements ApplicationAbstractForm {
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "业务系统")
    private Long subsystemId;
    
    @Attributes(title = "业务系统")
    private String subsystemCode;
    
    @Attributes(title = "业务系统")
    private String subsystemName;

    @Attributes(title = "应用类型" , type = FieldOptionsApplicationType.class)
    private String type ;
    
    @Attributes(title = "源码仓库类型", type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "构建版本")
    private String buildMainVersion;
    
    @Attributes(title = "代码仓库")
    private String vcsPath;
    
    @Attributes(title = "发布分支")
    private String releaseBranch;
    
    @Attributes(title = "是否收藏")
    private Boolean bookmarked;
    
    @Attributes(title = "构建服务", type = FieldSysBuildService.class)
    private String buildService;
    
    @Attributes(title = "代码分级", type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "是否有状态", type = FieldOptionsYesOrNo.class)
    private Integer stateless;
}
