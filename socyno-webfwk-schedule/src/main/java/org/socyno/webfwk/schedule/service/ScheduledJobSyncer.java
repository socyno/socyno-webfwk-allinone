package org.socyno.webfwk.schedule.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.socyno.webfwk.schmodel.ScheduleJobConfig;
import org.socyno.webfwk.schmodel.ScheduleJobConfigDetail;
import org.socyno.webfwk.schmodel.ScheduleJobTrigger;
import org.socyno.webfwk.schmodel.TenantBasicInfo;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.DataUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduledJobSyncer {
    
    @Autowired
    private Scheduler scheduler;
    
    private void updateTenantSchedules(String tenantCode) throws Exception {
        List<ScheduleJobConfig> scheduleJobs = ScheduleJobService.DEFAULT.queryTenantSchedules(tenantCode);
        Set<JobKey> tenantLatestJobs = new HashSet<JobKey>();
        if (scheduleJobs != null && scheduleJobs.size() > 0) {
            /**
             * 添加或更新计划任务
             */
            for (ScheduleJobConfig job : scheduleJobs) {
                if (job == null) {
                    continue;
                }
                try {
                    job.setTenant(tenantCode);
                    tenantLatestJobs.add(createOrUpdateSchedule(tenantCode, job));
                } catch (Exception e) {
                    warnForTenantScheduleResetFailure(tenantCode, e, job);
                }
            }
        }
        /**
         * 删除已移除的计划任务
         */
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(tenantCode))) {
            if (tenantLatestJobs.contains(jobKey)) {
                continue;
            }
            try {
                scheduler.deleteJob(jobKey);
            } catch (Exception e) {
                warnForTenantScheduleRemoveFailure(tenantCode, jobKey, e);
            }
        }
    }
    
    private JobKey createOrUpdateSchedule(@NonNull String tenant, final ScheduleJobConfig job) throws Exception {
        final String jobGroup = tenant;
        JobKey jobKey = JobKey.jobKey(job.getId() + "", jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(DataUtil.randomGuid(), jobKey.getGroup());
        
        JobDetail jobDetail;
        CronTrigger replacedTrigger = null;
        List<TriggerKey> removedTriggers = new ArrayList<TriggerKey>();
        if ((jobDetail = scheduler.getJobDetail(jobKey)) == null) {
            jobDetail = JobBuilder.newJob(ScheduledJob.class).withIdentity(jobKey).build();
        } else {
            List<? extends Trigger> currentTriggers;
            if ((currentTriggers = scheduler.getTriggersOfJob(jobKey)) != null) {
                for (Trigger trigger : currentTriggers) {
                    if (trigger == null) {
                        continue;
                    }
                    if (replacedTrigger == null && (trigger instanceof CronTrigger)) {
                        replacedTrigger = (CronTrigger) trigger;
                        continue;
                    }
                    removedTriggers.add(trigger.getKey());
                }
            }
        }
        jobDetail.getJobDataMap().put(ScheduledJob.TenantScheduleJobKey, job);
        if (replacedTrigger == null) {
            Set<Trigger> triggers = new HashSet<Trigger>();
            triggers.add(TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).startNow().build());
            scheduler.scheduleJob(jobDetail, triggers, true);
        }
        /**
         * 当执行计划表达式发生变化时，需要替换掉触发器
         */
        else if (!StringUtils.equalsIgnoreCase(replacedTrigger.getCronExpression(), job.getCronExpression())) {
            scheduler.rescheduleJob(replacedTrigger.getKey(), TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).startNow().build());
        }
        for (TriggerKey removed : removedTriggers) {
            try {
                scheduler.unscheduleJob(removed);
            } catch (Exception e) {
                log.error("租户({})的计划任务({}:{})移除失效触发器({})失败.", tenant, job.getId(), job.getTitle(), removed);
            }
        }
        log.info("租户({})的计划任务({}:{}){}成功，执行计划表达式为 : {}", tenant, job.getId(), job.getTitle(),
                replacedTrigger == null ? "创建" : "更新",
                job.getCronExpression());
        return jobKey;
    }
    
    private void warnForTenantScheduleResetFailure(String tenant, Throwable e, ScheduleJobConfig... jobs) {
        log.error("添加或更新计划任务失败：租户 = {}, JobConfig = {}, 异常 = {}", tenant, jobs, CommonUtil.stringifyStackTrace(e));
    }
    
    private void warnForTenantScheduleRemoveFailure(String tenant, JobKey jobKey, Throwable e) {
        log.error("移除计划任务失败：租户 = {}, JobKey = {}, 异常 = {}", tenant, jobKey, CommonUtil.stringifyStackTrace(e));
    }
    
    synchronized public void run() throws Exception {
        /**
         * 查询各租户的计划任务配置
         */
        for (TenantBasicInfo tenant : ScheduleJobService.DEFAULT.queryEnabledTenants()) {
            try {
                updateTenantSchedules(tenant.getCode());
            } catch (Exception ex) {
                warnForTenantScheduleResetFailure(tenant.getCode(), ex);
            }
        }
        
        /**
         * 确保计划任务的服务已经启动
         */
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }
    
    /**
     * 获取当前的任务清单
     */
    public List<ScheduleJobConfigDetail> getCurrentJobs() throws Exception {
        List<JobExecutionContext> executionContexts;
        Set<JobKey> runningJobKeys = new HashSet<JobKey>();
        if ((executionContexts = scheduler.getCurrentlyExecutingJobs()) != null && executionContexts.size() > 0) {
            for (JobExecutionContext jobContext : executionContexts) {
                runningJobKeys.add(jobContext.getJobDetail().getKey());
            }
        }
        JobDetail jobDetail;
        GroupMatcher<JobKey> matcher = GroupMatcher.anyGroup();
        Set<JobKey> allJobKeys = scheduler.getJobKeys(matcher);
        List<ScheduleJobConfigDetail> jobCongifDetails = new ArrayList<ScheduleJobConfigDetail>();
        for (final JobKey jobKey : allJobKeys) {
            if ((jobDetail = scheduler.getJobDetail(jobKey)) == null) {
                continue;
            }
            ScheduleJobConfigDetail jobCongifDetail = new ScheduleJobConfigDetail();
            for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                if (trigger == null) {
                    continue;
                }
                if (jobCongifDetail.getTriggers() == null) {
                    jobCongifDetail.setTriggers(new ArrayList<ScheduleJobTrigger>());
                }
                ScheduleJobTrigger jobTrigger = new ScheduleJobTrigger();
                jobTrigger.setKey(trigger.getKey().toString());
                if (trigger instanceof CronTrigger) {
                    jobTrigger.setCronExpression(((CronTrigger)trigger).getCronExpression());
                }
                jobTrigger.setNextFireTime(trigger.getNextFireTime());
                jobTrigger.setPreviousFireTime(trigger.getPreviousFireTime());
                jobTrigger.setState(scheduler.getTriggerState(trigger.getKey()).name());
                jobCongifDetail.getTriggers().add(jobTrigger);
            }
            
            Object jobConfig = jobDetail.getJobDataMap().get(ScheduledJob.TenantScheduleJobKey);
            if (jobConfig != null && (jobConfig instanceof ScheduleJobConfig)) {
                jobCongifDetail.setId(((ScheduleJobConfig)jobConfig).getId());
                jobCongifDetail.setTenant(((ScheduleJobConfig)jobConfig).getTenant());
                jobCongifDetail.setTitle(((ScheduleJobConfig)jobConfig).getTitle());
                jobCongifDetail.setCronExpression(((ScheduleJobConfig)jobConfig).getCronExpression());
            }
            if (runningJobKeys.contains(jobKey)) {
                jobCongifDetail.setRunning(true);
            }
            jobCongifDetails.add(jobCongifDetail);
        }
        return jobCongifDetails;
    }
}
