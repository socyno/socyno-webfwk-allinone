package org.socyno.webfwk.module.app.form;


import java.util.List;

import org.socyno.webfwk.module.app.form.FieldApplicationNamespace.OptionApplicationNamespace;

public interface ApplicationWithNamespaces extends ApplicationAbstractForm {
    
    public List<OptionApplicationNamespace> getDeployNamespaces();
    
    public void setDeployNamespaces(List<OptionApplicationNamespace> deployNamespaces);
    
}
