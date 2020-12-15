package org.socyno.webfwk.module.application;

import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;

@Data
public class ApplicationRuntimeEvnVersions {
    
    @Attributes(title = "部署环境")
    private String env;
    
    @Attributes(title = "部署环境显示")
    private String display;
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "部署版本号")
    private String[] versions;
    
    public void setVersions(String versions) {
        this.versions = CommonUtil.split(versions, "[,]", CommonUtil.STR_NONBLANK);
    }
}
