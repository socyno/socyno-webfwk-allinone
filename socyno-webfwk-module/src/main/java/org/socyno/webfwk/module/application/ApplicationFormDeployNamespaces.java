package org.socyno.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.module.application.FieldApplicationNamespace.OptionApplicationNamespace;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace;

@Getter
@Setter
@ToString
public class ApplicationFormDeployNamespaces extends ApplicationFormSimple implements ApplicationWithNamespaces {
    
    @Attributes(title = "部署机组清单", type = FieldDeployNamespace.class)
    private List<OptionApplicationNamespace> deployNamespaces;
    
}
