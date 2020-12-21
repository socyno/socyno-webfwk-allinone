package org.socyno.webfwk.state.module.job;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;
import org.socyno.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
public class SystemJobFormDetail extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemJobService.getInstance().getStates();
        }
        
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
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

    @Attributes(title = "默认参数")
    private String defaultParams;
}
