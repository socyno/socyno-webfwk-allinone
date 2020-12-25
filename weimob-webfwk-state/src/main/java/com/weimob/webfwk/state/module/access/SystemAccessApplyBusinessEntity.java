package com.weimob.webfwk.state.module.access;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemRole;
import com.weimob.webfwk.state.field.OptionSystemBusiness;
import com.weimob.webfwk.state.field.OptionSystemRole;
import com.weimob.webfwk.state.module.access.SystemAccessApplyFormSimple.AccessType;

import lombok.Data;

@Data
public class SystemAccessApplyBusinessEntity {
    
    @Attributes(title = "单号")
    private Long applyId;
    
    @Attributes(title = "角色", required = true, type = FieldSystemRole.class)
    private OptionSystemRole role;
    
    @Attributes(title = "类型", required = true, type = SystemAccessApplyFormSimple.FieldOptionsAccessType.class)
    private String accessType;
    
    @Attributes(title = "业务", required = true, type = FieldSystemBusinessAccessApply.class)
    private OptionSystemBusiness business;
    
    public boolean isBusinessEntity() {
        return !AccessType.SYSTEM.getValue().equalsIgnoreCase(accessType);
    }
    
}
