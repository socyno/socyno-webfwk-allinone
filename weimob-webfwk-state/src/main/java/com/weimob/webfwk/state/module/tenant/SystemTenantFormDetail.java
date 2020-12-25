package com.weimob.webfwk.state.module.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.field.FieldSystemFeature;
import com.weimob.webfwk.state.field.OptionSystemFeature;
import com.weimob.webfwk.util.state.field.FieldTableView;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户详情信息")
public class SystemTenantFormDetail extends SystemTenantFormDefault {
    
    public static class FieldOptionDbInfo extends FieldTableView {
        @Override
        public Class<?> getListItemCreationFormClass() {
            return SystemTenantDbInfo.class;
        }
        
    }
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemTenantService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "授权功能", type = FieldSystemFeature.class)
    private List<OptionSystemFeature> features;
    
    @Attributes(title = "数据连接", type = FieldOptionDbInfo.class)
    private List<SystemTenantDbInfo> databases;
}
