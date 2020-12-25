package com.weimob.webfwk.util.exception;

public class NamingConflictedException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    public NamingConflictedException(String message) {
        this(message, null);
    }
    
    public NamingConflictedException(String message, Throwable e) {
        super(message, e);
    }
    
}
