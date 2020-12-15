package org.socyno.webfwk.module.sysjob;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldDateTime;
import org.socyno.webfwk.util.state.field.FieldText;

@Data
public class SystemJobFormDetail implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemJobService.getInstance().getStates();
        }
        
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "名称")
    private String title;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "最大并发数")
    private Integer concurrentAllowed;
    
    @Attributes(title = "服务类")
    private String serviceClass;
    
    @Attributes(title = "服务实例")
    private String serviceInstance;
    
    @Attributes(title = "执行计划")
    private String cronExpression;
    
    @Attributes(title = "参数模型", type = FieldText.class)
    private String serviceParametersForm;
    
    @Attributes(title = "运行任务数")
    private Integer runningTasks;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt;
    
    @Attributes(title = "创建人编号")
    private Long createdBy;
    
    @Attributes(title = "创建人账户")
    private String createdCodeBy;
    
    @Attributes(title = "创建人姓名")
    private String createdNameBy;

    @Attributes(title = "默认参数")
    private String defaultParams;
}
