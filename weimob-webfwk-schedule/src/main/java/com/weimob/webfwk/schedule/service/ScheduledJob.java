package com.weimob.webfwk.schedule.service;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.weimob.webfwk.schmodel.ScheduleJobConfig;
import com.weimob.webfwk.schmodel.ScheduleJobStatus;
import com.weimob.webfwk.schmodel.ScheduleJobTask;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisallowConcurrentExecution
public class ScheduledJob implements Job {
    
    public final static String TenantScheduleJobKey = "job";
    
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ScheduleJobTask jobTask = null;
        ScheduleJobStatus status = null;
        ScheduleJobConfig configs = null;
        JobDetail job = jobExecutionContext.getJobDetail();
        try {
            configs = (ScheduleJobConfig) job.getJobDataMap().get(TenantScheduleJobKey);
            jobTask = ScheduleJobService.DEFAULT.executeScheduleJob(configs.getTenant(), configs.getId());
            log.info("启动租户自动化计划任务成功, 开始等待结束 ：实例 = {}， 配置 = {}", jobTask, configs);
            status = ScheduleJobService.DEFAULT.waitingForScheduleJobFinished(configs.getTenant(), configs.getId(),
                    jobTask.getTaskId());
            log.info("执行租户自动化计划任务结束 ：实例 = {}， 配置 = {}， 状态 = {}", jobTask, configs, status);
        } catch (Exception e) {
            log.error("执行租户自动化计划任务异常 ：实例 = {}， 配置 = {}，异常 = {}", jobTask, configs, CommonUtil.stringifyStackTrace(e));
            throw new JobExecutionException(e);
        }
    }
}
