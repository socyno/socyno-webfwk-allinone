package org.socyno.webfwk.schmodel;

import java.util.Date;

import lombok.Data;

@Data
public class ScheduleJobTrigger {
    
    private String key;
    
    private String state;
    
    private String cronExpression;
    
    private Date nextFireTime;
    
    private Date previousFireTime;
}
