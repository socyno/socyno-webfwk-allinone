package org.socyno.webfwk.module.vcs.common;

import org.socyno.webfwk.util.exception.MessageException;

public class VcsFailToRestAppRepoException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    public VcsFailToRestAppRepoException(String message) {
        this(message, null);
    }
    
    public VcsFailToRestAppRepoException(String message, Throwable e) {
        super(String.format("应用仓库初始化或重置授权： %s", message), e);
    }
    
}
