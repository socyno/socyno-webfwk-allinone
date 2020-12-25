package com.weimob.webfwk.state.module.menu;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemAuths;
import com.weimob.webfwk.state.field.OptionSystemAuth;

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
