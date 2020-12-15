package org.socyno.webfwk.module.application;

import java.util.Date;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;

@Data
public class ApplicationRuntimeStatusNodeItem {
    
    @Attributes(title = "应用编号")
    private Long appId;
    
    @Attributes(title = "应用名称")
    private String appName;
    
    @Attributes(title = "部署名称")
    private String appContext;
    
    @Attributes(title = "部署环境")
    private String envName;
    
    @Attributes(title = "部署环境")
    private String envDisplay;
    
    @Attributes(title = "集群名称")
    private String clusterName;
    
    @Attributes(title = "集群类型")
    private String clusterType;
    
    @Attributes(title = "集群类型")
    private String clusterTypeDisplay;
    
    @Attributes(title = "命名空间")
    private String namespaceName;
    
    @Attributes(title = "节点名称")
    private String instanceName;
    
    @Attributes(title = "节点地址")
    private String instanceHost;
    
    @Attributes(title = "状态")
    private String status;
    
    @Attributes(title = "版本号")
    private String version;
    
    @Attributes(title = "最新更新")
    private Date lastUpdated;
}
