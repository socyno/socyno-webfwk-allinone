package org.socyno.webfwk.module.deploy.environment;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.FieldBooleanYesOrNo;
import org.socyno.webfwk.util.state.field.FieldDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

@Getter
@Setter
@ToString
public class DeployEnvironmentFormSimple implements AbstractDeployEnvironmentForm, AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return DeployEnvironmentService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "代码")
    private String name;
    
    @Attributes(title = "名称")
    private String display;
    
    @Attributes(title = "Sars接口地址")
    private String sarsInterfaze;
    
    @Attributes(title = "中控机")
    private String controlHost;
    
    @Attributes(title = "是否支持数据源管理", type = FieldBooleanYesOrNo.class)
    private boolean dataSourceSupported;
    
    @Attributes(title = "创建人")
    private Long createdBy;
    
    @Attributes(title = "创建人")
    private String createdCodeBy;
    
    @Attributes(title = "创建人")
    private String createdNameBy;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt;
}
