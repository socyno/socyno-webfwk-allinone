package com.weimob.webfwk.state.module.feature;

import java.util.List;

import com.weimob.webfwk.state.field.OptionSystemAuth;

public interface SystemFeatureWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
