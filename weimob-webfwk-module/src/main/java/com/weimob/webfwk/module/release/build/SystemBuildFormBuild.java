package com.weimob.webfwk.module.release.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import com.weimob.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
@Attributes(title = "启动应用构建")
public class SystemBuildFormBuild extends StateFormBasicInput {
    
    @Attributes(title = "租户代码", required = true)
    private String tenant;
    
    @Attributes(title = "应用名称", required = true)
    private String appliction;
    
    @Attributes(title = "应用类型", required = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "构建版本", required = true)
    private String buildVersion;
    
    @Attributes(title = "代码仓库", required = true)
    private String vcsPath;
    
    @Attributes(title = "代码分支", required = true)
    private String vcsRefsName;
    
    @Attributes(title = "代码版本", required = true)
    private String vcsRevision;
}
