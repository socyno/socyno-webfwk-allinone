package com.weimob.webfwk.state.module.menu;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemMenuPane;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加菜单目录")
public class SystemMenuDirFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径", required = true)
    private String path;
    
    @Attributes(title = "排序", required = true)
    private Integer order;
    
    @Attributes(title = "面板", required = true, type = FieldSystemMenuPane.class)
    private Long paneId;
}
