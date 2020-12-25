package com.weimob.webfwk.executor.api.chartmail;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.executor.abs.AbstractJobParameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChartMailParams implements AbstractJobParameters {
    
    @Attributes(title = "表单ID")
    private Long formId;
    
}
