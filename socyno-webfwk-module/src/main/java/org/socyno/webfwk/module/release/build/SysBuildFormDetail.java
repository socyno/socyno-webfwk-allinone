package org.socyno.webfwk.module.release.build;

import java.util.List;

import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsApplicationType;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

@Getter
@Setter
@ToString
public class SysBuildFormDetail implements AbstractStateForm {
    
    public static class FieldSysBuildOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SysBuildService.DEFAULT.getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "名称")
    private String code;
    
    @Attributes(title = "类型", type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "标题")
    private String title;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "状态", type = FieldSysBuildOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
}
