package org.socyno.webfwk.module.sysjob;

import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemJobFormStatus extends StateFormBasicInput {
    
    @Attributes(title = "任务编号", required = true)
    private Long jobId;
    
}
