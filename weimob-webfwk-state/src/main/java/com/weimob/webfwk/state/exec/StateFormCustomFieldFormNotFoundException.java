package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;


@Getter
public class StateFormCustomFieldFormNotFoundException extends MessageException {
    private static final long serialVersionUID = 1L;

    private final String formName;
    
    public StateFormCustomFieldFormNotFoundException(String formName) {
        super(String.format("表单排版定义(%s)文件不存在.", CommonUtil.ifNull(formName, "")));
        this.formName = formName;
    }
}