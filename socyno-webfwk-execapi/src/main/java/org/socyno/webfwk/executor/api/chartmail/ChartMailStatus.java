package org.socyno.webfwk.executor.api.chartmail;

import org.socyno.webfwk.executor.model.JobBasicStatus;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Attributes(title = "图表发送邮件状态")
public class ChartMailStatus extends JobBasicStatus {
    
    
}
