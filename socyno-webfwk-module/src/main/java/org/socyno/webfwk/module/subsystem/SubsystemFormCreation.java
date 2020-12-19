package org.socyno.webfwk.module.subsystem;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubsystemFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "代码", required = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
