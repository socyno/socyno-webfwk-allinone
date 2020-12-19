package org.socyno.webfwk.state.module.menu;

import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加菜单导航")
public class SystemMenuPaneFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径", required = true)
    private String path;
    
    @Attributes(title = "排序", required = true)
    private Integer order;
}
