package com.weimob.webfwk.state.module.user;

import java.util.List;

import com.weimob.webfwk.state.field.OptionSystemUserAuth;

public interface SystemUserWithAuths {
    
    public List<OptionSystemUserAuth> getAuths() ;
    
    public void setAuths(List<OptionSystemUserAuth> auths) ;
    
}
