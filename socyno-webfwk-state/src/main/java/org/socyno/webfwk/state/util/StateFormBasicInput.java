package org.socyno.webfwk.state.util;
import org.socyno.webfwk.state.abs.AbstractStateFormInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFormBasicInput implements AbstractStateFormInput {

    @Attributes(title = "编号", required = true, readonly = true)
    private Long id;
    
    @Attributes(title = "版本", required = true, readonly = true)
    private Long revision;
}