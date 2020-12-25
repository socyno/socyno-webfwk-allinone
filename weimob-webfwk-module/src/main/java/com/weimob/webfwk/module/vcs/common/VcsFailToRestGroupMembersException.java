package com.weimob.webfwk.module.vcs.common;

import com.weimob.webfwk.util.exception.MessageException;

public class VcsFailToRestGroupMembersException extends MessageException {

    private static final long serialVersionUID = 1L;
    
    public VcsFailToRestGroupMembersException(String message) {
        this(message, null);
    }
    
    public VcsFailToRestGroupMembersException(String message, Throwable e) {
        super(String.format("应用授权组初始化或重置： %s", message), e);
    }
    
}
