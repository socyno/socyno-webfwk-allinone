package org.socyno.webfwk.module.app.form;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.socyno.webfwk.state.util.StateFormEventResultSimpleView;
import org.socyno.webfwk.util.state.field.FieldTableView;

@Getter
@Setter
public class ApplicationRuntimeStatusView extends StateFormEventResultSimpleView {

    public ApplicationRuntimeStatusView() throws Exception {
        super();
    }

    @Attributes(title = "运行状态" , type = FieldTableView.class )
    private List<ApplicationRuntimeStatusNodeItem> nodeItems ;

}
