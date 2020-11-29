package org.socyno.webfwk.module.dynamicoption;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.util.state.field.FieldText;


@Getter
@Setter
@ToString
public class SystemFieldOptionEntity {
    
    @Attributes(title = "选项编号", readonly = true)
    private Long id;
    
    @Attributes(title = "路径/名称")
    private String classPath;
    
    @Attributes(title = "选项值", required = true)
    private String value;
    
    @Attributes(title = "选项显示", required = true)
    private String display;
    
    @Attributes(title = "选项分组")
    private String group;
    
    @Attributes(title = "选项图标")
    private String icon;
    
    @Attributes(title = "选项样式")
    private String style;
    
    @Attributes(title = "选项状态", required = true, type = FieldDisabled.class)
    private String disabled;
    
    @Attributes(title = "选项类别")
    private String category;
    
    @Attributes(title = "选项配置", type = FieldText.class)
    private String properties;
    
    @Getter
    public enum OptionDisabled{
        /**
         *  是否禁用
         */
        TRUE("true","禁用"),
        FALSE("false" ,"启用"),
        ;
        private final String code;
        private final String display;

        OptionDisabled(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }

    public static class FieldDisabled extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (OptionDisabled type : OptionDisabled.values()) {
                    add(FieldSimpleOption.create(type.getCode(),type.getDisplay()));
                }
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
}
