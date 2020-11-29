package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.CommonUtil;

public class StateFormSubmitEventResultException extends MessageException {
    private static final long serialVersionUID = 1L;

    private final String form;
    private final String event;
    
    public StateFormSubmitEventResultException(String form, String event) {
        this.form = form;
        this.event = event;
    }
    
    @Override
    public String getMessage() {
        return String.format("表单(%s)的创建操作(%s)返回要求必须为新建的表单编号。", 
                CommonUtil.ifNull(form, ""), CommonUtil.ifNull(event, ""));
    }
}
