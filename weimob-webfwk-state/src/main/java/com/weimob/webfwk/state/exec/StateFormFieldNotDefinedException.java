package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class StateFormFieldNotDefinedException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    private String formName;
    private String fieldName;
    
    public StateFormFieldNotDefinedException(String form, String field) {
        this.formName = form;
        this.fieldName = field;
    }
    
    @Override
    public String getMessage() {
        return String.format("表单字段未定义（form=%s, field=%s）.", 
                CommonUtil.ifNull(formName, ""), CommonUtil.ifNull(fieldName, ""));
    }
}
