package com.weimob.webfwk.state.exec;

import com.weimob.webfwk.util.exception.MessageException;

public class StateFormEmptyTargetStateException extends MessageException {

    private static final long serialVersionUID = 1L;
 
    public StateFormEmptyTargetStateException() {
        super("表单操作目标状态值不可为空。");
    }
}
