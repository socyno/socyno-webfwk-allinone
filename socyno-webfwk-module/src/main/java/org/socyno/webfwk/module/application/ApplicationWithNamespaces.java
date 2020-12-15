package org.socyno.webfwk.module.application;


import java.util.List;

import org.socyno.webfwk.module.application.FieldApplicationNamespace.OptionApplicationNamespace;

public interface ApplicationWithNamespaces extends ApplicationAbstractForm {
    
    public List<OptionApplicationNamespace> getDeployNamespaces();
    
    public void setDeployNamespaces(List<OptionApplicationNamespace> deployNamespaces);
    
}
