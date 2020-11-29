package org.socyno.webfwk.util.exception;

public class MissingUserException extends HttpResponseException {
    private static final long serialVersionUID = 1L;
    
    public MissingUserException() {
        this("未知用户信息, 不可执行任何操作！");
    }
    
    public MissingUserException(String message) {
        super(401, message);
    }
}
