package org.socyno.webfwk.module.systenant;

import java.util.List;

import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace.OptionDeployClusterNamespace;
import org.socyno.webfwk.state.field.FieldSystemFeature;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantDbInfo;
import org.socyno.webfwk.util.state.field.FieldTableView;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户详情信息")
public class SystemTenantDetail extends SystemTenantSimple {
    
    public static class FieldOptionDbInfo extends FieldTableView {
        @Override
        public Class<?> getListItemCreationFormClass() {
            return SystemTenantDbInfo.class;
        }
        
    }
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemTenantService.DEFAULT.getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "授权功能", type = FieldSystemFeature.class)
    private List<OptionSystemFeature> features;
    
    @Attributes(title = "部署命名空间", type = FieldDeployNamespace.class)
    private List<OptionDeployClusterNamespace> namespaces;
    
    @Attributes(title = "数据连接", type = FieldOptionDbInfo.class)
    private List<SystemTenantDbInfo> databases;
}
