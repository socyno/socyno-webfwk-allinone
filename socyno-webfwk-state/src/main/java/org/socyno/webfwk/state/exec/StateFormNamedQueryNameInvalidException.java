package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.CommonUtil;

public class StateFormNamedQueryNameInvalidException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    private String name;
    
    public StateFormNamedQueryNameInvalidException(String name) {
        this.name = name;
    }
    
    @Override
    public String getMessage() {
        return String.format("给定的预定义通用表单查询名称规范(%s)", CommonUtil.ifNull(name , ""));
    }
}
