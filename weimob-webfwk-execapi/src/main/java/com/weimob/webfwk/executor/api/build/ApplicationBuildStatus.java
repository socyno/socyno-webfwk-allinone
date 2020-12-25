package com.weimob.webfwk.executor.api.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.executor.model.JobBasicStatus;

@Setter
@Getter
@ToString
@Attributes(title = "应用构建状态")
public class ApplicationBuildStatus extends JobBasicStatus {
    
    @Attributes(title = "应用名称", required = true)
    private String application;
    
    @Attributes(title = "构建版本")
    private String buildVersion;
    
    @Attributes(title = "发布包地址")
    private String packageUrl;
}
