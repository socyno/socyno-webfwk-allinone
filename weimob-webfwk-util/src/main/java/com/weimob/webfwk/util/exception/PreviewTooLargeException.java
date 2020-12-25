package com.weimob.webfwk.util.exception;

public class PreviewTooLargeException extends HttpResponseException {
    private static final long serialVersionUID = 1L;

    public PreviewTooLargeException() {
        super(500, "Preview File Too Large.");
    }
}
