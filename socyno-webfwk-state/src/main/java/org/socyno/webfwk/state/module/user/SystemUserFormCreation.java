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
@Attributes(title = "添加系统用户")
public class SystemUserFormCreation extends StateFormBasicInput {

    @Attributes(title = "用户", required = true)
    private String  username;

    @Attributes(title = "姓名", required = true)
    private String  display;
    
    @Attributes(title = "邮箱", required = true)
    private String  mailAddress;
    
    @Attributes(title = "职务")
    private String  title;

    @Attributes(title = "部门")
    private String  department;
    
    @Attributes(title = "手机")
    private String  mobile;
    
    @Attributes(title = "座机")
    private String  telphone;
    
    @Attributes(title = "新的密码", required = true, type = FieldPassword.class)
    private String newPassword;
    
    @Attributes(title = "密码确认", required = true, type = FieldPassword.class)
    private String confirmPassword;
}
