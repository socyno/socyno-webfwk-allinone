package com.weimob.webfwk.util.vcs.gitlab;

import java.io.IOException;

public class IllegalResponseException extends IOException {
    
    public IllegalResponseException() {
        super();
    }

    public IllegalResponseException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
    
}