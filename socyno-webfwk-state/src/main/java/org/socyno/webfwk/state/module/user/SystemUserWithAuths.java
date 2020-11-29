package org.socyno.webfwk.state.module.user;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemUserAuth;

public interface SystemUserWithAuths {
    
    public List<OptionSystemUserAuth> getAuths() ;
    
    public void setAuths(List<OptionSystemUserAuth> auths) ;
    
}
