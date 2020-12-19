package org.socyno.webfwk.module.sysjob;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemJobFormCreate extends StateFormBasicInput {
    
    @Attributes(title = "名称", required = true)
    private String title;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "最大并发数", required = true)
    private Integer concurrentAllowed;
    
    @Attributes(title = "服务类", required = true)
    private String serviceClass;
    
    @Attributes(title = "服务实例", required = true)
    private String serviceInstance;
    
    @Attributes(title = "执行计划")
    private String cronExpression;
    
    @Attributes(title = "参数模型", type = FieldText.class)
    private String serviceParametersForm;

    @Attributes(title = "默认参数")
    private String defaultParams;
}
