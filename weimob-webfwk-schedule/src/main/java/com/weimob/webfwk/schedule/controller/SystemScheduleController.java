package com.weimob.webfwk.schedule.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.schedule.service.ScheduledJobSyncer;
import com.weimob.webfwk.util.remote.R;

@RestController
@RequestMapping(value = "/api/schedules")
public class SystemScheduleController {
    
    @Autowired
    private ScheduledJobSyncer scheduledJobSyncer;
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list() throws Exception {
        return R.ok().setData(scheduledJobSyncer.getCurrentJobs());
    }
    
    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public R sync() throws Exception {
        scheduledJobSyncer.run();
        return R.ok();
    }
}
