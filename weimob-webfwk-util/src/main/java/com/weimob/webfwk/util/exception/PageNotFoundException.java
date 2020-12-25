package com.weimob.webfwk.util.exception;

public class PageNotFoundException extends HttpResponseException {
    private static final long serialVersionUID = 1L;

    public PageNotFoundException() {
        super(404, "Page Not Found.");
    }
}
