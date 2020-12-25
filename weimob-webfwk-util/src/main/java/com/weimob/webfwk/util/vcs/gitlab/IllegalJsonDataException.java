package com.weimob.webfwk.util.vcs.gitlab;

public class IllegalJsonDataException extends IllegalResponseException {

    private static final long serialVersionUID = 1L;
    
    public IllegalJsonDataException() { 
        super();
    }

    public IllegalJsonDataException(String message) {
        super(message);
    }
    
}