package org.socyno.webfwk.module.app.form;

import java.util.List;

import org.socyno.webfwk.module.app.form.FieldApplicationNamespace.OptionApplicationNamespace;
import org.socyno.webfwk.state.util.StateFormEventResultSimpleView;
import org.socyno.webfwk.util.state.field.FieldTableView;

import lombok.Getter;
import lombok.Setter;

import com.github.reinert.jjschema.Attributes;

@Setter
@Getter
public class ApplicationDeployNamespacesView extends StateFormEventResultSimpleView {
    
    public ApplicationDeployNamespacesView() throws Exception {
        super();
    }
    
    @Attributes(title = "部署机组清单", type = FieldTableView.class)
    private List<OptionApplicationNamespace> deployNamespaces;
    
}
