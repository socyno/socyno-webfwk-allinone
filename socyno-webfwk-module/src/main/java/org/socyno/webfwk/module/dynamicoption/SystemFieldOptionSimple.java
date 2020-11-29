package org.socyno.webfwk.module.dynamicoption;

import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemFieldOptionSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemFieldOptionService.DEFAULT.getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    @Attributes(title = "编号", readonly = true)
    private Long id;

    @Attributes(title = "版本", readonly = true)
    private Long revision;

    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;

    @Attributes(title = "路径/名称")
    private String classPath;
    
    @Attributes(title = "选项清单")
    private List<SystemFieldOptionEntity> options;
    
}
