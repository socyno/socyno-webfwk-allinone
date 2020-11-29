package org.socyno.webfwk.executor.model;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateActionTriggerForm {
    
    @Attributes(title = "编号")
    private Long id;
    
    public StateActionTriggerForm() {
        this(null);
    }
    
    public StateActionTriggerForm(Long id) {
        this.id = id;
    }
}
