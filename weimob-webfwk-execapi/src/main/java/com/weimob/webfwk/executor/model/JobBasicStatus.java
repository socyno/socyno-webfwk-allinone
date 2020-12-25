package com.weimob.webfwk.executor.model;

import lombok.Data;

import java.util.Date;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.executor.abs.AbstractJobStatus;

@Data
public class JobBasicStatus implements AbstractJobStatus {
    
    @Attributes(title = "任务编号", required = true)
    private Long taskId;
    
    @Attributes(title = "任务状态", required = true)
    private JobStatusEnum status;
    
    @Attributes(title = "创建人", required = true)
    private String createdBy;
    
    @Attributes(title = "创建时间", required = true)
    private Date createdAt;
    
    @Attributes(title = "启动开始")
    private Date startedAt;
    
    @Attributes(title = "结束时间")
    private Date completedAt;
}
