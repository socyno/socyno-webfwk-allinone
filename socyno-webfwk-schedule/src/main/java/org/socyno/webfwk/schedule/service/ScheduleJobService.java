package org.socyno.webfwk.schedule.service;

import java.net.URISyntaxException;
import java.util.List;

import org.socyno.webfwk.schmodel.ScheduleJobConfig;
import org.socyno.webfwk.schmodel.ScheduleJobStatus;
import org.socyno.webfwk.schmodel.ScheduleJobTask;
import org.socyno.webfwk.schmodel.TenantBasicInfo;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.remote.RestClient;
import org.socyno.webfwk.util.tool.CommonUtil;

public class ScheduleJobService {
    
    public static final ScheduleJobService DEFAULT = new ScheduleJobService();
    
    private String getBackendRemoteUrl() {
//      return "http://localhost:8080/webfwk-backend";
        return CommonUtil.ifBlank(ContextUtil.getConfigTrimed("system.schedule.backend.service.url"),
                "http://localhost:8080/webfwk-backend");
    }
    
    private int getBackendTimeoutMS() {
        return CommonUtil.parseInteger(ContextUtil.getConfigTrimed("system.schedule.backend.service.timeout"), 60000);
    }
    
    public RestClient getBackendService() throws URISyntaxException {
        RestClient restfulClient = new RestClient(getBackendRemoteUrl());
        restfulClient.setTimeoutMS(getBackendTimeoutMS());
        return restfulClient;
    }
    
    public static class TenantPagedList extends PagedList<TenantBasicInfo> {
        
    }
    
    public List<TenantBasicInfo> queryEnabledTenants() throws Exception {
        return getBackendService().get(TenantPagedList.class, "/api/internal/tenants/enabled/1").getList();
    }
    
    public static class TenantSchedulePageList extends PagedList<ScheduleJobConfig> {
        
    }
    
    public List<ScheduleJobConfig> queryTenantSchedules(String tenant) throws Exception {
        return getBackendService().get(TenantSchedulePageList.class,
                String.format("/api/internal/tenants/%s/schedules/1", tenant)).getList();
    }
    
    public ScheduleJobTask executeScheduleJob(String tenant, long scheduleId) throws Exception {
        return getBackendService().post(ScheduleJobTask.class,
                String.format("/api/internal/tenants/%s/schedules/%s/execute", tenant, scheduleId));
    }
    
    public ScheduleJobStatus waitingForScheduleJobFinished(String tenant, long scheduleId, long jobId)
            throws Exception {
        ScheduleJobStatus status;
        long waitingMaxTimeoutMS = 3600000L;
        final long waitingIntervalMS = 20000L;
        while (waitingMaxTimeoutMS > 0) {
            status = getBackendService().get(ScheduleJobStatus.class,
                    String.format("/api/internal/tenants/%s/schedules/%s/jobs/%s/status", tenant, scheduleId, jobId));
            if ("RUNNING".equalsIgnoreCase(status.getStatus()) || "PENDING".equalsIgnoreCase(status.getStatus())) {
                Thread.sleep(waitingIntervalMS);
                waitingMaxTimeoutMS -= waitingIntervalMS;
                continue;
            }
            return status;
        }
        throw new RuntimeException(String.format("等待任务结束超时（等待时长为 %s 毫秒）", waitingMaxTimeoutMS));
    }
}
