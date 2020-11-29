package org.socyno.webfwk.executor.api.chartmail;

import org.socyno.webfwk.executor.abs.AbstractJobParameters;

import com.github.reinert.jjschema.Attributes;
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
