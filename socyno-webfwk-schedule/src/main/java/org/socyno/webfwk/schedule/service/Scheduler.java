package org.socyno.webfwk.schedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class Scheduler {
    
    @Autowired
    private ScheduledJobSyncer scheduledJobSycner;
    
    /**
     * 同步计划任务的配置信息
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    public void syncScheduledJobs() throws Exception {
        scheduledJobSycner.run();
    }
}
