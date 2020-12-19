package org.socyno.webfwk.module.sysjob;

import org.socyno.webfwk.state.util.StateFormBasicForm;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemJobFormStatus extends StateFormBasicForm {
    
    @Attributes(title = "任务编号", position = 1010, required = true)
    private Long jobId;
    
}
