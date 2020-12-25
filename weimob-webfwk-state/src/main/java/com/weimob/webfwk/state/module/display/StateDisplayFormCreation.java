package com.weimob.webfwk.state.module.display;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

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
