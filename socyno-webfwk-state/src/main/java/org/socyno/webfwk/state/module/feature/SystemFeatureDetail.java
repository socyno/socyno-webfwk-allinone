package org.socyno.webfwk.state.module.feature;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.FieldSystemRole;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.field.OptionSystemRole;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统功能详情")
public class SystemFeatureDetail extends SystemFeatureSimple {
    @Attributes(title = "接口/操作", position = 1210, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
    
    @Attributes(title = "授权的角色", position = 1220, readonly = true, type = FieldSystemRole.class)
    private List<OptionSystemRole> roles;
}
