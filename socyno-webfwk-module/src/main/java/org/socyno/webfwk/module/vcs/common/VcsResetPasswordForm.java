package org.socyno.webfwk.module.vcs.common;

import lombok.Data;

@Data
public class VcsResetPasswordForm {
    
    private String username;
    
    private String password;
    
    private String newPassword;
}
