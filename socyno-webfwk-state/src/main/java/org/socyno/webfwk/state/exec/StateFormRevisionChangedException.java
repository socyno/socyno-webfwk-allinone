package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.exception.MessageException;

import lombok.Getter;

@Getter
public class StateFormRevisionChangedException extends MessageException {
    private static final long serialVersionUID = 1L;
    private final Long oldRevision;
    private final Long newRevision;
    private final AbstractStateForm form;
    
    public StateFormRevisionChangedException(AbstractStateForm form, Long oldRevision, Long newRevision) {
        this.form = form;
        this.oldRevision = oldRevision;
        this.newRevision = newRevision;
    }
    
    @Override
    public String getMessage() {
        return "表单实体已被变更，请刷新页面(或重新获取)。";
    }
}
