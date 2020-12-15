package org.socyno.webfwk.state.module.user;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemUserAuth;
import org.socyno.webfwk.state.field.OptionSystemUserAuth;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户详情")
public class SystemUserFormDetail extends SystemUserFormDefault implements SystemUserWithSecurities, SystemUserWithAuths {
    
    @Attributes(title = "手机")
    private String mobile;
    
    @Attributes(title = "座机")
    private String telphone;
    
    @Attributes(title = "授权", type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> auths;
}
