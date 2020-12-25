package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class StateFormNotFoundException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    private Long formId;
    private String formName;
    
    public StateFormNotFoundException(String name, Long formId) {
        this.formName = name;
        this.formId = formId;
    }
    
    @Override
    public String getMessage() {
        return String.format("给定的表单（name=%s, formId=%s）数据不存在.", 
                CommonUtil.ifNull(formName, ""), CommonUtil.ifNull(formId, ""));
    }
}
