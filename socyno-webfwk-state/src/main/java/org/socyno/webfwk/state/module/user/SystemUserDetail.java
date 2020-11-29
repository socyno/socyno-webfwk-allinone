package org.socyno.webfwk.state.module.user;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.FieldSystemUserAuth;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUserAuth;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户详情")
public class SystemUserDetail extends SystemUserSimple implements SystemUserWithSecurities,
        SystemUserWithAuths, SystemUserWithManagerEntity {
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemUserService.DEFAULT.getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "手机")
    private String mobile;
    
    @Attributes(title = "座机")
    private String telphone;
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private OptionSystemUser managerEntity;
    
    @Attributes(title = "授权", type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> auths;
}
