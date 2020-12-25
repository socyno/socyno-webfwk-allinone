package com.weimob.webfwk.util.exception;

import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class HttpResponseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private int status = 500;
    
    public HttpResponseException(int status) {
        this(status, null);
    }
    
    public HttpResponseException(String message) {
        this(500, message);
    }
    
    public HttpResponseException(int status, String message) {
        this(status, message, null);
    }
    
    public HttpResponseException(String message, Throwable ex) {
        this(500, message, ex);
    }
    
    public HttpResponseException(int status, String message, Throwable ex) {
        super(CommonUtil.ifBlank(message, "Internal System Error."), ex);
        this.status = status;
    }
}
