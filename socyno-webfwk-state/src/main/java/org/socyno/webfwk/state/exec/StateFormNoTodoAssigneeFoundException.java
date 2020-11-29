package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.CommonUtil;

public class StateFormNoTodoAssigneeFoundException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    private Long formId;
    private String formName;
    private String formEvent;
    
    public StateFormNoTodoAssigneeFoundException(String name, Long formId, String event) {
        this.formName = name;
        this.formId = formId;
        this.formEvent = event;
    }
    
    @Override
    public String getMessage() {
        return String.format("未获取到待办事项的处理人员, 无法创建通用表单待办事项（formName=%s, formId=%s, formEvent=%s)",
                CommonUtil.ifNull(formName, ""), CommonUtil.ifNull(formId, ""), CommonUtil.ifNull(formEvent, ""));
    }
}
