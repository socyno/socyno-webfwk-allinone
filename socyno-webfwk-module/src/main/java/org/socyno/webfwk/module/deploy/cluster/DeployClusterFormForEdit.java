package org.socyno.webfwk.module.deploy.cluster;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.module.deploy.cluster.DeployClusterFormDefaultForm.FieldOptionsType;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class DeployClusterFormForEdit extends BasicStateForm {
    @Attributes(title = "代码", required = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String title;
    
    @Attributes(title = "类型", required = true, type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "环境", required = true, type = FieldDeployEnvironment.class)
    private String environment;
    
    @Attributes(title = "接口地址")
    private String apiService;
    
    @Attributes(title = "接口证书", type = FieldText.class)
    private String apiClientCert;
    
    @Attributes(title = "接口令牌", type = FieldText.class)
    private String apiClientToken;
    
    @Attributes(title = "集群说明", type = FieldText.class)
    private String description;
}
