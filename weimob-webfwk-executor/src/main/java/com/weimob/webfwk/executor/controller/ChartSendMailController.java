package com.weimob.webfwk.executor.controller;

import org.springframework.web.bind.annotation.*;

import com.weimob.webfwk.executor.api.chartmail.ChartMailParams;
import com.weimob.webfwk.executor.api.chartmail.ChartMailStatus;
import com.weimob.webfwk.executor.model.StateActionTriggerData;
import com.weimob.webfwk.executor.model.StateActionTriggerForm;
import com.weimob.webfwk.executor.service.AsyncTaskService;
import com.weimob.webfwk.executor.service.ContextBackendService;
import com.weimob.webfwk.util.exception.PageNotFoundException;
import com.weimob.webfwk.util.model.SimpleLock;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.service.AbstractAsyncTaskService.AsyncTaskExecutor;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.SimpleLogger;

import java.io.FileOutputStream;

@RestController
@RequestMapping(value = "/reportChartSendMail")
public class ChartSendMailController {
    
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public R start(final @RequestBody ChartMailParams chartParams) throws Exception {
        
        long taskId = AsyncTaskService.DEFAULT.execute(new AsyncTaskExecutor("图表发送邮件", chartParams.getFormId() + "",
                String.format("图表:%s 发送邮件", chartParams.getFormId())) {
            
            @Override
            public boolean execute(FileOutputStream logsOutputStream, String logsDir) throws Exception {
                try {
                    StateActionTriggerData triggerAction = new StateActionTriggerData();
                    triggerAction.setForm(new StateActionTriggerForm(chartParams.getFormId()));
                    ContextBackendService.getInstance().triggerAction("apply_charts_simple", "chart_send_mail",
                            triggerAction);
                    SimpleLogger.logInfo(logsOutputStream, "[成功]发送邮件成功");
                    return true;
                } catch (Exception e) {
                    SimpleLogger.logError(logsOutputStream, "[异常]发送邮件失败 ：%s", CommonUtil.stringifyStackTrace(e));
                    return false;
                }
            }
        });
        
        return R.ok().setData(taskId);
    }
    
    @RequestMapping(value = "/status/{taskId}", method = RequestMethod.GET)
    public R status(@PathVariable Long taskId) throws Exception {
        
        SimpleLock task;
        if (taskId == null || (task = AsyncTaskService.DEFAULT.getStatus(taskId)) == null) {
            throw new PageNotFoundException();
        }
        ChartMailStatus status = AsyncTaskService.toJobBasicStatus(ChartMailStatus.class, task);
        
        ClassUtil.checkFormRequiredAndOpValue(status);
        return R.ok().setData(status);
    }
}
