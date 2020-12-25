package com.weimob.webfwk.state.module.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemUserFormLogin {
    
    private String username;
    
    private String password;
    
    private String proxied;
    
    private String token;
}
