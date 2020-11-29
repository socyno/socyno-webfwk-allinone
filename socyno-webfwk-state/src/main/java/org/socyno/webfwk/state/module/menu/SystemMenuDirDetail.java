package org.socyno.webfwk.state.module.menu;

import org.socyno.webfwk.state.field.FieldSystemMenuPane;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "菜单目录详情")
public class SystemMenuDirDetail extends SystemMenuDirSimple {
    
    @Attributes(title = "面板编号", type = FieldSystemMenuPane.class)
    private Long paneId;
}
