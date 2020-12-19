package org.socyno.webfwk.state.module.menu;

import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单导航")
public class SystemMenuPaneFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemMenuPaneService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long   revision;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径")
    private String path;
    
    @Attributes(title = "排序")
    private Integer order;
}
