package org.socyno.webfwk.state.module.user;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.FieldSystemUserAuth;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUserAuth;
import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统用户")
public class SystemUserFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "用户", readonly = true)
    private String username;
    
    @Attributes(title = "姓名", required = true)
    private String display;
    
    @Attributes(title = "邮箱", required = true)
    private String mailAddress;
    
    @Attributes(title = "手机")
    private String mobile;
    
    @Attributes(title = "座机")
    private String telphone;
    
    @Attributes(title = "职务")
    private String title;
    
    @Attributes(title = "部门")
    private String department;
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private OptionSystemUser manager;
    
    @Attributes(title = "授权", type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> auths;
}
