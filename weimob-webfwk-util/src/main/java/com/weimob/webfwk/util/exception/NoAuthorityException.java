package com.weimob.webfwk.util.exception;

public class NoAuthorityException extends HttpResponseException {
    
    private static final long serialVersionUID = 1L;
    
    public NoAuthorityException() {
        this("No Authority Error");
    }
    
    public NoAuthorityException(String message) {
        super(403, message);
    }
}
