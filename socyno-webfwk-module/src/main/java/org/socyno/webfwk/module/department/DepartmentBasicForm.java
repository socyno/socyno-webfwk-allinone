package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

@Data
public class DepartmentBasicForm implements DepartmentAbstractForm {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return DepartmentService.DEFAULT.getStates();
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "负责人")
    private Long ownerId;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
