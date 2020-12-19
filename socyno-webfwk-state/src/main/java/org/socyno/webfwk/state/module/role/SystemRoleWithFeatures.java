package org.socyno.webfwk.state.module.role;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemFeature;

public interface SystemRoleWithFeatures {

    public List<OptionSystemFeature> getFeatures();
    
    public void setFeatures(List<OptionSystemFeature> features);
    
}
