package com.weimob.webfwk.util.exception;

import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class MessageException extends HttpResponseException {
    private String messagePreffix = null;
    
    public MessageException() {
       super("Internal Server Error");
    }
    
    public MessageException(String message) {
        super(message);
    }
    
    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getMessage() {
        String preffix = CommonUtil.ifNull(getMessagePreffix(), "");
        String message = CommonUtil.ifNull(super.getMessage(), "");
        return preffix.isEmpty() ? message : (
            message.isEmpty() ? preffix
                : String.format("%s:%s", preffix, message)
        );
    }
}
