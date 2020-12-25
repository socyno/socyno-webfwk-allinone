package com.weimob.webfwk.module.application;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.FieldApplicationNamespace.OptionApplicationNamespace;
import com.weimob.webfwk.state.util.StateFormEventResultSimpleView;
import com.weimob.webfwk.util.state.field.FieldTableView;

@Setter
@Getter
public class ApplicationDeployNamespacesView extends StateFormEventResultSimpleView {
    
    public ApplicationDeployNamespacesView() throws Exception {
        super();
    }
    
    @Attributes(title = "部署机组清单", type = FieldTableView.class)
    private List<OptionApplicationNamespace> deployNamespaces;
    
}
