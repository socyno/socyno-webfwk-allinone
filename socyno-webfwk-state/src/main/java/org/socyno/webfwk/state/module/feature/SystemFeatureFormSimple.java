package org.socyno.webfwk.state.module.feature;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemFeatureFormSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemFeatureService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号", readonly = true)
    private Long   id;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "创建人")
    private String createdBy;
    
    @Attributes(title = "创建时间")
    private Date   createdAt;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "版本", readonly = true)
    private Long   revision;
}