package org.socyno.webfwk.module.systenant;

import java.util.List;

import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespaceWithAdd;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace.OptionDeployClusterNamespace;
import org.socyno.webfwk.module.systenant.SystemTenantDetail.FieldOptionDbInfo;
import org.socyno.webfwk.module.systenant.SystemTenantDetail.FieldOptionsState;
import org.socyno.webfwk.state.field.FieldSystemFeature;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantDbInfo;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑租户信息")
public class SystemTenantForEdition implements AbstractSystemTenant {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "租户代码", readonly = true)
    private String code;
    
    @Attributes(title = "源码空间", readonly = true)
    private String codeNamespace;
    
    @Attributes(title = "程序组织", readonly = true)
    private String codeLibGroup;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
    
    @Attributes(title = "授权功能", type = FieldSystemFeature.class)
    private List<OptionSystemFeature> features;
    
    @Attributes(title = "部署命名空间", type = FieldDeployNamespaceWithAdd.class)
    private List<OptionDeployClusterNamespace> namespaces;
    
    @Attributes(title = "数据连接", required = true, type = FieldOptionDbInfo.class)
    private List<SystemTenantDbInfo> databases;
}
