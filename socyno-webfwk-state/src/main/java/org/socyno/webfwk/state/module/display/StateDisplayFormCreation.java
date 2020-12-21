package org.socyno.webfwk.state.module.display;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.*;

@Getter
@Setter
@ToString
public class StateDisplayFormCreation extends StateFormBasicInput  {

    @Attributes(title = "路径", required = true)
    private String name ;

    @Attributes(title = "显示", required = true)
    private String display ;

    @Attributes(title = "备注",type = FieldText.class, required = true)
    private String remark ;

}
