package org.socyno.webfwk.state.module.notify;

import org.socyno.webfwk.util.exception.MessageException;

public class SystemNotifiyTemplateNotFoundException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    public SystemNotifiyTemplateNotFoundException(String template) {
        super(String.format("No such notify template found: %s", template));
    }
    
}
