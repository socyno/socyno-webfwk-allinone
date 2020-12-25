package com.weimob.webfwk.state.module.role;

import java.util.List;

import com.weimob.webfwk.state.field.OptionSystemFeature;

public interface SystemRoleWithFeatures {

    public List<OptionSystemFeature> getFeatures();
    
    public void setFeatures(List<OptionSystemFeature> features);
    
}
