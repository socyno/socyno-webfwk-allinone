package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class StateFormNamedQueryNotFoundException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    private String formName;
    private String queryName;
    
    public StateFormNamedQueryNotFoundException(String formName, String queryName) {
        this.formName = formName;
        this.queryName = queryName;
    }
    
    @Override
    public String getMessage() {
        return String.format("给定的表单预定义查询（formName=%s, queryName=%s）不存在.", 
                CommonUtil.ifNull(formName, ""), CommonUtil.ifNull(queryName, ""));
    }
}
