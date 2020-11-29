package org.socyno.webfwk.schmodel;

import lombok.Data;

@Data
public class ScheduleJobConfig {
    
    private long id;
    
    private String title;
    
    private String tenant;
    
    private String cronExpression;
}
