package org.socyno.webfwk.state.module.feature;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemAuth;

public interface SystemFeatureWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
