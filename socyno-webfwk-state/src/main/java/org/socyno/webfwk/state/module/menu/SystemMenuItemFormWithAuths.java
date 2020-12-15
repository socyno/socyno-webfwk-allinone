package org.socyno.webfwk.state.module.menu;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemAuth;

public interface SystemMenuItemFormWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
