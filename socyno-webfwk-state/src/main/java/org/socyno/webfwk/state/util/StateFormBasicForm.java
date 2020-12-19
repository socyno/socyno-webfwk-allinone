package org.socyno.webfwk.state.util;
import org.socyno.webfwk.state.basic.AbstractStateForm;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFormBasicForm implements AbstractStateForm {

    @Attributes(title = "编号", required = true, readonly = true)
    private Long id;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "版本", required = true, readonly = true)
    private Long revision;
}