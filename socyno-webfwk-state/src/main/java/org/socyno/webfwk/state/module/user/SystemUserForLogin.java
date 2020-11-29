package org.socyno.webfwk.state.module.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemUserForLogin {
    
    private String username;
    
    private String password;
    
    private String proxied;
    
    private String token;
}
