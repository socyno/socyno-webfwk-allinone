package com.weimob.webfwk.state.module.config;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.field.FieldStringAllowOrDenied;
import com.weimob.webfwk.state.util.StateFormBasicSaved;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "系统参数配置")
public class SystemConfigFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemConfigService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "键", required = true)
    private String name;
    
    @Attributes(title = "值", required = true, type = FieldText.class)
    private String value;
    
    @Attributes(title = "外部访问", required = true, type = FieldStringAllowOrDenied.class)
    private String external;
    
    @Attributes(title = "备注", required = true, type = FieldText.class)
    private String comment;
    
}
