package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class StateFormActionNotFoundException extends MessageException {

    private static final long serialVersionUID = 1L;

    private final String form;
    private final String event;
    
    public StateFormActionNotFoundException(String form, String event) {
        this.form = form;
        this.event = event;
    }
    
    @Override
    public String getMessage() {
        return String.format("表单操作未定义（form=%s, event=%s）", 
                CommonUtil.ifNull(form, ""), CommonUtil.ifNull(event, ""));
    }
}
