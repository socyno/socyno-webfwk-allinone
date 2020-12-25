package com.weimob.webfwk.state.module.menu;

import java.util.List;

import com.weimob.webfwk.state.field.OptionSystemAuth;

public interface SystemMenuItemFormWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
