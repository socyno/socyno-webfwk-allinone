package org.socyno.webfwk.state.module.user;
 
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldPassword;

import com.github.reinert.jjschema.Attributes;

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
