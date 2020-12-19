package org.socyno.webfwk.module.productline;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormCreation extends StateFormBasicForm {
    
    @Attributes(title = "代码", required = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private Long owner;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
