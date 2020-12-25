package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.state.abs.AbstractStateFormInput;
import com.weimob.webfwk.util.exception.MessageException;

import lombok.Getter;

@Getter
public class StateFormRevisionChangedException extends MessageException {
    private static final long serialVersionUID = 1L;
    private final Long oldRevision;
    private final Long newRevision;
    private final AbstractStateFormInput form;
    
    public StateFormRevisionChangedException(AbstractStateFormInput form, Long oldRevision, Long newRevision) {
        this.form = form;
        this.oldRevision = oldRevision;
        this.newRevision = newRevision;
    }
    
    @Override
    public String getMessage() {
        return "表单实体已被变更，请刷新页面(或重新获取)。";
    }
}
