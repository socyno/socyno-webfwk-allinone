package org.socyno.webfwk.state.module.option;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.util.state.field.FieldText;


@Getter
@Setter
@ToString
public class DynamicFieldOptionEntity {
    
    @Attributes(title = "路径", readonly = true)
    private Long classPath;
    
    @Attributes(title = "选项类别")
    private String category;
    
    @Attributes(title = "选项分组")
    private String group;
    
    @Attributes(title = "选项值", required = true)
    private String value;
    
    @Attributes(title = "选项显示", required = true)
    private String display;
    
    @Attributes(title = "是否禁用", required = true)
    private boolean disabled;
    
    @Attributes(title = "选项配置", type = FieldText.class)
    private String properties;
}
