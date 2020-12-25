package com.weimob.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.FieldApplicationNamespace.OptionApplicationNamespace;
import com.weimob.webfwk.module.deploy.cluster.FieldDeployNamespace;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "应用详情信息")
public class ApplicationFormDefault extends ApplicationFormSimple implements ApplicationWithNamespaces {
    
    @Attributes(title = "部署机组清单", type = FieldDeployNamespace.class)
    private List<OptionApplicationNamespace> deployNamespaces;
    
    @Attributes(title = "部署机组概要")
    private List<DeployEnvNamespaceSummarySimple> deployNamespaceSummaries;
    
}
