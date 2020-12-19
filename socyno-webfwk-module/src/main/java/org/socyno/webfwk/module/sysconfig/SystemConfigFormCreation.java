package org.socyno.webfwk.module.sysconfig;

import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemConfigFormCreation extends StateFormBasicForm  {

    @Attributes(title = "键", required = true, position = 1001)
    private String name ;

    @Attributes(title = "值", required = true, position = 1002 ,type = FieldText.class)
    private String value ;

    @Attributes(title = "备注", position = 1003 ,type = FieldText.class)
    private String comment ;

}
