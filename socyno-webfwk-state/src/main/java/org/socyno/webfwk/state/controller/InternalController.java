package org.socyno.webfwk.state.controller;

import org.socyno.webfwk.state.module.job.SystemJobFormDetail;
import org.socyno.webfwk.state.module.job.SystemJobFormStatus;
import org.socyno.webfwk.state.module.job.SystemJobQueryDefault;
import org.socyno.webfwk.state.module.job.SystemJobService;
import org.socyno.webfwk.state.module.tenant.SystemTenantQueryDefault;
import org.socyno.webfwk.state.module.tenant.SystemTenantService;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.util.StateFormDynamicForm;
import org.socyno.webfwk.state.util.StateFormEventResultWebSocketViewLink;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonElement;


/**
 * 内部使用的接口。
 * 
 * <b>注意：此处定义的接口禁止开放之 gateway 上对外使用。</b>
 *
 */
public class InternalController {
    
    private void sudoToInternalJobUser(String tenant) throws Exception {
        SystemUserService.getInstance().forceSuToUser(String.format("internal_job@%s", tenant));
    }
    
    /**
     * 查询有效的租户名称清单
     */
    @RequestMapping(value = "/tenants/enabled/{page}", method = RequestMethod.GET)
    public R queryEnabledTenants(@PathVariable long page) throws Exception {
        return R.ok().setData(SystemTenantService.getInstance().listForm(SystemTenantService.QUERIES.DEFAULT,
                new SystemTenantQueryDefault(page, 100).setDisableIncluded(false)));
    }
    
    /**
     * 查询指定租户的计划任务清单
     */
    @RequestMapping(value = "/tenants/{tenant}/schedules/{page}", method = RequestMethod.GET)
    public R queryTenantSchedules(@PathVariable String tenant, @PathVariable long page) throws Exception {
        sudoToInternalJobUser(tenant);
        return R.ok().setData(SystemJobService.getInstance().listForm(SystemJobService.QUERIES.DEFAULT,
                new SystemJobQueryDefault(page, 100).setOnlyScheduled(true)));
    }
    
    /**
     * 执行指定租户的指定计划任务
     */
    @RequestMapping(value = "/tenants/{tenant}/schedules/{scheduleId}/execute", method = RequestMethod.POST)
    public R executeTenantScheduleJob(@PathVariable String tenant, @PathVariable long scheduleId) throws Exception {
        sudoToInternalJobUser(tenant);
        SystemJobFormDetail schedule = SystemJobService.getInstance().getForm(scheduleId);
        StateFormDynamicForm form = new StateFormDynamicForm();
        form.setId(scheduleId);
        form.setRevision(schedule.getRevision());
        form.setJsonData(CommonUtil.fromJson("{}", JsonElement.class));
        return R.ok().setData(SystemJobService.getInstance().triggerAction(
                SystemJobService.EVENTS.Execute.getName(), form, StateFormEventResultWebSocketViewLink.class));
    }
    
    /**
     * 查询指定租户的计划任务实例的状态
     */
    @RequestMapping(value = "/tenants/{tenant}/schedules/{scheduleId}/jobs/{jobId}/status", method = RequestMethod.GET)
    public R queryTenantScheduledJobStatus(@PathVariable String tenant, @PathVariable long scheduleId,
            @PathVariable long jobId) throws Exception {
        sudoToInternalJobUser(tenant);
        SystemJobFormStatus form = new SystemJobFormStatus();
        form.setId(scheduleId);
        form.setJobId(jobId);
        return R.ok().setData(SystemJobService.getInstance().triggerAction(
                SystemJobService.EVENTS.JobStatus.getName(), form, SystemJobFormStatus.class));
    }
}
