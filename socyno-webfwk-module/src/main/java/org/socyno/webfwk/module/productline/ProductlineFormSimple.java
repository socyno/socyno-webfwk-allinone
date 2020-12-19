package org.socyno.webfwk.module.productline;

import java.util.List;

import org.socyno.webfwk.state.util.StateFormBasicSaved;
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
public class ProductlineFormSimple extends StateFormBasicSaved implements ProductlineFormAbstract {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ProductlineService.getInstance().getStates();
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "负责人")
    private Long ownerId;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
