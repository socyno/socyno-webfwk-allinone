package org.socyno.webfwk.state.module.user;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldPassword;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "修改用户密码")
public class SystemUserFormNewPassword implements AbstractStateForm {
    
    @Attributes(title = "编号", readonly = true)
    private Long  id;
    
    @Attributes(title = "状态", readonly = true)
    private String  state;

    @Attributes(title = "版本", readonly = true)
    private Long  revision;

    @Attributes(title = "密码", position = 1010, required = true, type = FieldPassword.class)
    private String password;

    @Attributes(title = "新的密码", position = 1020, required = true, type = FieldPassword.class)
    private String newPassword;
    
    @Attributes(title = "密码确认", position = 1030, required = true, type = FieldPassword.class)
    private String confirmPassword;
}
