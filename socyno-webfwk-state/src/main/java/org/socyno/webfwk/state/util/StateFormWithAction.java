package org.socyno.webfwk.state.util;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.util.tool.ClassUtil;

import lombok.Data;

@Data
public class StateFormWithAction<F extends AbstractStateFormBase> {
    
    private final F form;
    private final String formClass;
    private final List<StateFormActionDefinition> actions = new ArrayList<>();
    
    public StateFormWithAction(F form, List<StateFormActionDefinition> actions) throws Exception {
        this.form = form;
        this.formClass = form == null ? null : ClassUtil.classToJson(form.getClass()).toString();
        if (actions != null) {
            this.actions.addAll(actions);
        }
    }
}
