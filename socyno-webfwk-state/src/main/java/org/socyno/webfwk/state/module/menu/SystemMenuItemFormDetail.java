package org.socyno.webfwk.state.module.menu;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.OptionSystemAuth;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单详情")
public class SystemMenuItemFormDetail extends SystemMenuItemFormDefault implements SystemMenuItemFormWithAuths {
    
    @Attributes(title = "授权明细", type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
