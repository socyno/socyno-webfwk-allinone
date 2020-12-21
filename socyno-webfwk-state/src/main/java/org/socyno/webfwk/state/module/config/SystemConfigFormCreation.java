package org.socyno.webfwk.state.module.config;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemConfigFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "键", required = true)
    private String name;
    
    @Attributes(title = "值", required = true, type = FieldText.class)
    private String value;
    
    @Attributes(title = "备注", type = FieldText.class)
    private String comment;
    
}
