package org.socyno.webfwk.schmodel;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleJobConfigDetail extends ScheduleJobConfig {
    
    private boolean running;
    
    private List<ScheduleJobTrigger>  triggers;
}
