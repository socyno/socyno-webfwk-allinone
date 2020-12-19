package org.socyno.webfwk.module.subsystem;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;

public interface SubsystemAbstractForm extends AbstractStateFormBase {
    
    public String getCode();
    
    public String getName();
    
    public String getDescription();
}
