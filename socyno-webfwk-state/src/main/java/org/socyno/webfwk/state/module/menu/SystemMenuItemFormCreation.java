package org.socyno.webfwk.state.module.menu;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.FieldSystemMenuDir;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.util.StateFormBasicForm;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加系统菜单")
public class SystemMenuItemFormCreation extends StateFormBasicForm {
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径", required = true)
    private String path;
    
    @Attributes(title = "排序", required = true)
    private Integer order;
    
    @Attributes(title = "目录编号", required = true, type = FieldSystemMenuDir.class)
    private Long dirId;
    
    @Attributes(title = "授权明细", required = true, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
