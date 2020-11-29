package org.socyno.webfwk.util.exception;

public class TenantMissingException extends MessageException {
    private static final long serialVersionUID = 1L;

    public TenantMissingException(){
        this("未知租户信息, 不可执行任何操作！");
    }
    
    public TenantMissingException(String message){
        super(message);
    }

}
