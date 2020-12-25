package com.weimob.webfwk.executor.api.build;

import lombok.Data;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.executor.abs.AbstractJobParameters;

@Data
@Attributes(title = "应用构建表单")
public class ApplicationBuildParameters implements AbstractJobParameters {
    
    @Attributes(title = "租户代码", required = true)
    private String tenant;
    
    @Attributes(title = "命名空间", required = true)
    private String namespace;
    
    @Attributes(title = "包组织名", required = true)
    private String artifectGroupId;
    
    @Attributes(title = "应用名称", required = true)
    private String application;
    
    @Attributes(title = "应用类型", required = true)
    private String appType;
    
    @Attributes(title = "代码仓库", required = true)
    private String vcsPath;
    
    @Attributes(title = "代码分支", required = true)
    private String vcsRefsName;
    
    @Attributes(title = "代码版本", required = true)
    private String vcsRevision;
    
    @Attributes(title = "构建版本", required = true)
    private String buildVersion;
    
    @Attributes(title = "构建服务", required = true)
    private String buildService;
}
