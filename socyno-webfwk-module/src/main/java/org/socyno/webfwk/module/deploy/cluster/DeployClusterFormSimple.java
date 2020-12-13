package org.socyno.webfwk.module.deploy.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldDateTime;
import org.socyno.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

@Getter
@Setter
@ToString
public class DeployClusterFormSimple implements AbstractStateForm {

    @Getter
    public enum ClusterType {
        VM("vm" , "虚拟机"),
        K8S("k8s" , "K8S容器");

        private String name ;
        private String display ;

        ClusterType(String name ,String display){
            this.name = name ;
            this.display = display ;
        }
    }

    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return DeployClusterService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for(ClusterType clusterType : ClusterType.values()){
                    add(FieldSimpleOption.create(clusterType.getName(), clusterType.getDisplay()));
                }
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "名称")
    private String title;
    
    @Attributes(title = "类型", type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "环境", type = FieldDeployEnvironment.class)
    private String environment;
    
    @Attributes(title = "接口地址")
    private String apiService;
    
    @Attributes(title = "接口证书")
    private String apiClientCert;
    
    @Attributes(title = "接口Token")
    private String apiClientToken;
    
    @Attributes(title = "创建人")
    private Long createdBy;
    
    @Attributes(title = "创建人")
    private String createdCodeBy;
    
    @Attributes(title = "创建人")
    private String createdNameBy;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt;
    
    @Attributes(title = "集群说明", type = FieldText.class)
    private String description;
    
}
