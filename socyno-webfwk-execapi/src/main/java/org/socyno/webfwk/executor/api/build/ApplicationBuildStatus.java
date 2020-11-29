package org.socyno.webfwk.executor.api.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.executor.model.JobBasicStatus;

import com.github.reinert.jjschema.Attributes;

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
