package org.socyno.webfwk.module.sysjob;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemJobFormEdit extends BasicStateForm {
    
    @Attributes(title = "名称", position = 1010, required = true)
    private String title;
    
    @Attributes(title = "描述", position = 1020, type = FieldText.class)
    private String description;
    
    @Attributes(title = "最大并发数", position = 1030)
    private Integer concurrentAllowed;
    
    @Attributes(title = "服务类", position = 1100, required = true)
    private String serviceClass;
    
    @Attributes(title = "服务实例", position = 1110, required = true)
    private String serviceInstance;
    
    @Attributes(title = "执行计划", position = 1300)
    private String cronExpression;
    
    @Attributes(title = "参数模型", position = 1140, type = FieldText.class)
    private String serviceParametersForm;

    @Attributes(title = "默认参数", position = 1500)
    private String defaultParams;
    
}
