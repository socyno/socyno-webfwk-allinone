package org.socyno.webfwk.state.module.user;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Attributes(title = "系统用户清单")
public class SystemUserFormDefault extends SystemUserFormSimple implements SystemUserWithManagerEntity {
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private OptionSystemUser managerEntity;
    
}
