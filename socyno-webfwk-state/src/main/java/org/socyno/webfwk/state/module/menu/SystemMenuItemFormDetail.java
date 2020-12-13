package org.socyno.webfwk.state.module.menu;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.FieldSystemMenuDir;
import org.socyno.webfwk.state.field.OptionSystemAuth;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单详情")
public class SystemMenuItemFormDetail extends SystemMenuItemFormDefault {
    
    @Attributes(title = "目录编号")
    private Long dirId;
    
    @Attributes(title = "面板编号", type = FieldSystemMenuDir.class)
    private Long paneId;
    
    @Attributes(title = "授权明细", type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
