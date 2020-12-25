package com.weimob.webfwk.state.module.job;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

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
