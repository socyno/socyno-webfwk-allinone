package com.weimob.webfwk.state.module.user;
 
import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldPassword;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "修改用户密码")
public class SystemUserFormNewPassword extends StateFormBasicInput {
    
    @Attributes(title = "密码", required = true, type = FieldPassword.class)
    private String password;

    @Attributes(title = "新的密码", required = true, type = FieldPassword.class)
    private String newPassword;
    
    @Attributes(title = "密码确认", required = true, type = FieldPassword.class)
    private String confirmPassword;
}
