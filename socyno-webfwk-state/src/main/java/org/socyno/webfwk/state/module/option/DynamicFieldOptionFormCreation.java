package org.socyno.webfwk.state.module.option;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
public class DynamicFieldOptionFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "路径", required = true)
    private String classPath;
    
    @Attributes(title = "描述", required = true)
    private String description;
    
    @Attributes(title = "选项清单", required = true, type = FieldDynamicFieldOptionEntityCreate.class)
    private List<DynamicFieldOptionEntity> values;    
    
}
