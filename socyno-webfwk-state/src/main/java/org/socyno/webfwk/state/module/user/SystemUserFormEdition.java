package org.socyno.webfwk.state.module.user;

import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.FieldSystemUserAuth;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUserAuth;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统用户")
public class SystemUserFormEdition implements AbstractStateForm {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "用户", position = 1010, readonly = true)
    private String username;
    
    @Attributes(title = "姓名", position = 1020, required = true)
    private String display;
    
    @Attributes(title = "邮箱", position = 1030, required = true)
    private String mailAddress;
    
    @Attributes(title = "手机", position = 1040)
    private String mobile;
    
    @Attributes(title = "座机", position = 1050)
    private String telphone;
    
    @Attributes(title = "职务", position = 1060)
    private String title;
    
    @Attributes(title = "部门", position = 1070)
    private String department;
    
    @Attributes(title = "直属领导", position = 1080, type = FieldSystemUser.class)
    private OptionSystemUser manager;
    
    @Attributes(title = "授权", position = 1300, type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> auths;
}
