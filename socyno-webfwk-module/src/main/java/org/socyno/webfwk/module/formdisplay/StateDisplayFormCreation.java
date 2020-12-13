package org.socyno.webfwk.module.formdisplay;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.*;

@Getter
@Setter
@ToString
public class StateDisplayFormCreation extends BasicStateForm  {

    @Attributes(title = "路径", required = true, position = 1001)
    private String name ;

    @Attributes(title = "显示", required = true, position = 1002 )
    private String display ;

    @Attributes(title = "备注", position = 1003 ,type = FieldText.class, required = true)
    private String remark ;

}
