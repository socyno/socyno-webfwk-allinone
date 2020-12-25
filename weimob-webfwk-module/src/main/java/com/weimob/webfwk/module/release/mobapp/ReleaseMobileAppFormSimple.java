package com.weimob.webfwk.module.release.mobapp;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;
import com.weimob.webfwk.state.util.StateFormBasicSaved;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用配置")
public class ReleaseMobileAppFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ReleaseMobileAppService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsAppStoreType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create("ios", "ios"));
                add(FieldSimpleOption.create("android", "android"));
            }
        };
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = ReleaseMobileAppFormDetail.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "应用名", required = true, position = 1001)
    private String applicationName;
    
    @Attributes(title = "系统类型", required = true, position = 1002, type = ReleaseMobileAppFormDetail.FieldOptionsAppStoreType.class)
    private String storeType;
    
    @Attributes(title = "审批人", required = true, position = 1003, type = FieldSystemUser.class)
    private OptionSystemUser approver;
    
}
