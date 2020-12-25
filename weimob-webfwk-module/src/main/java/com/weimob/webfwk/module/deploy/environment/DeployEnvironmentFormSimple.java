package com.weimob.webfwk.module.deploy.environment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
public class DeployEnvironmentFormSimple extends StateFormBasicSaved implements AbstractDeployEnvironmentForm, AbstractStateFormBase {
    
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
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "代码")
    private String name;
    
    @Attributes(title = "名称")
    private String display;
}
