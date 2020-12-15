package org.socyno.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeployEnvNamespaceSummarySimple {
    
    @Attributes(title = "环境")
    private String envName;
    
    @Attributes(title = "环境名称")
    private String envDisplay;
    
    @Attributes(title = "副本总数")
    private Integer replicas;
    
    @Attributes(title = "应用总数")
    private Integer appTotal;
    
    @Attributes(title = "涉及集群")
    private Integer clusterTotal;
    
    @Attributes(title = "涉及机组")
    private Integer namespaceTotal;
    
}
