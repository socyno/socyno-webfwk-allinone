package org.socyno.webfwk.module.subsystem;

import org.socyno.webfwk.state.basic.AbstractStateForm;

public interface SubsystemAbstractForm extends AbstractStateForm {
    
    public String getCode();
    
    public String getName();
    
    public String getDescription();
}
