package org.socyno.webfwk.util.exception;

public class AbstractMethodUnimplimentedException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    public AbstractMethodUnimplimentedException() {
        super("未实现的抽象或静态方法调用。");
    }
    
}
