package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.CommonUtil;

public class StateFormSubmitEventTargetException extends MessageException {
    private static final long serialVersionUID = 1L;

    private final String form;
    private final String event;
    
    public StateFormSubmitEventTargetException(String form, String event) {
        this.form = form;
        this.event = event;
    }
    
    @Override
    public String getMessage() {
        return String.format("表单(%s)的创建操作(%s)结果未明确定义初始状态。", 
                CommonUtil.ifNull(form, ""), CommonUtil.ifNull(event, ""));
    }
}
