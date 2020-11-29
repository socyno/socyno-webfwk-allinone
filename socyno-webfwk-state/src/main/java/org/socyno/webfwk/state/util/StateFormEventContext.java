package org.socyno.webfwk.state.util;


import org.socyno.webfwk.state.basic.AbstractStateForm;

import lombok.Data;

@Data
public class StateFormEventContext<F extends AbstractStateForm> {
    private final String message;
    private final F form;
    
    public StateFormEventContext(String message, F form) {
        this.form = form;
        this.message = message;
    }
}
