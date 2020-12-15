package org.socyno.webfwk.module.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class DeployEnvNamespaceSummaryDetail {
    
    @Data
    @Accessors(chain = true)
    public static class ClusterInfo {
        
        private String type;
        
        private String name;
    }
    
    @Data
    @Accessors(chain = true)
    public static class NamespaceInfo {
        
        private Long id;
        
        private String name;
    }
    
    @Attributes(title = "当前的统计范围", description = "当前统计的信息范围标的编号，可能是单个应用业务系统或产品线的编号")
    private Long targetScopeId;
    
    @Attributes(title = "环境")
    private String envName;
    
    @Attributes(title = "环境名称")
    private String envDisplay;
    
    @Attributes(title = "副本总数")
    private Integer replicas = 0;
    
    @Attributes(title = "应用清单")
    private final Set<Long> appIds = new HashSet<>();
    
    @Attributes(title = "集群清单")
    private final Set<ClusterInfo> clusters = new HashSet<>();
    
    @Attributes(title = "机组清单")
    private final Set<NamespaceInfo> namespaces = new HashSet<>();
    
    public void addClusterInfo(String type, String name) {
        clusters.add(new ClusterInfo().setType(type).setName(name));
    }
    
    public void addClusterInfo(Collection<ClusterInfo> clusters) {
        if (clusters == null || clusters.size() <= 0) {
            return;
        }
        for (ClusterInfo c : clusters) {
            if (c == null) {
                continue;
            }
            this.clusters.add(c);
        }
    }
    
    public void addNamespaceInfo(Long id, String name) {
        namespaces.add(new NamespaceInfo().setId(id).setName(name));
    }
    
    public void addNamespaceInfo(Collection<NamespaceInfo> namespaces) {
        if (namespaces == null || namespaces.size() <= 0) {
            return;
        }
        for (NamespaceInfo n : namespaces) {
            if (n == null) {
                continue;
            }
            this.namespaces.add(n);
        }
    }
    
    public void addAppId(Long appId) {
        if (appId == null) {
            return;
        }
        this.appIds.add(appId);
    }
    
    public void addAppId(Collection<Long> appIds) {
        if (appIds == null || appIds.size() <= 0) {
            return;
        }
        for (Long appId : appIds) {
            if (appId == null) {
                continue;
            }
            this.appIds.add(appId);
        }
    }
    
    public void addReplicas(Integer replicas) {
        this.replicas = CommonUtil.ifNull(this.replicas, 0) + CommonUtil.ifNull(replicas, 0);
    }
    
    public DeployEnvNamespaceSummarySimple asSimple() {
        return new DeployEnvNamespaceSummarySimple()
                .setEnvName(getEnvName())
                .setEnvDisplay(getEnvDisplay())
                .setReplicas(getReplicas())
                .setAppTotal(getAppIds().size())
                .setClusterTotal(getClusters().size())
                .setNamespaceTotal(getNamespaces().size());
    }
    
    public static List<DeployEnvNamespaceSummarySimple> toSimple(Collection<DeployEnvNamespaceSummaryDetail> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeployEnvNamespaceSummarySimple> result = new ArrayList<>();
        for (DeployEnvNamespaceSummaryDetail d : details) {
            if (d == null) {
                continue;
            }
            result.add(d.asSimple());
        }
        return result;
    }
}
