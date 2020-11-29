package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import lombok.Data;

import java.util.List;

import org.socyno.webfwk.util.state.field.FieldText;

@Data
public class SubsystemBasicForm implements SubsystemAbstractForm, FieldOption {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SubsystemService.DEFAULT.getStates();
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
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;

    @Override
    public String getOptionValue() {
        return "" + getId();
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s:%s", getCode(), getName());
    }
    
    @Override
    public void setOptionValue(String value) {
        setId(new Long(value));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && SubsystemBasicForm.class.equals(obj.getClass()) &&
                this.getOptionValue().equals(((SubsystemBasicForm) obj).getOptionValue());
    }

    @Override
    public int hashCode() {
        return getOptionValue().hashCode();
    }
}
