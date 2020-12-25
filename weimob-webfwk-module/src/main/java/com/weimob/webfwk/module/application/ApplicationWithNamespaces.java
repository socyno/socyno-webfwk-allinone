package com.weimob.webfwk.module.application;


import java.util.List;

import com.weimob.webfwk.module.application.FieldApplicationNamespace.OptionApplicationNamespace;

public interface ApplicationWithNamespaces extends ApplicationFormAbstract {
    
    public List<OptionApplicationNamespace> getDeployNamespaces();
    
    public void setDeployNamespaces(List<OptionApplicationNamespace> deployNamespaces);

    public List<DeployEnvNamespaceSummarySimple> getDeployNamespaceSummaries();

    public void setDeployNamespaceSummaries(List<DeployEnvNamespaceSummarySimple> deployNamespaceSummaries);
    
}
