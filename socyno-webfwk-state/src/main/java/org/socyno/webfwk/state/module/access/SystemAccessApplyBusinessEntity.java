package org.socyno.webfwk.state.module.access;

import org.socyno.webfwk.state.field.FieldSystemRole;
import org.socyno.webfwk.state.field.OptionSystemBusiness;
import org.socyno.webfwk.state.field.OptionSystemRole;
import org.socyno.webfwk.state.module.access.SystemAccessApplyFormSimple.AccessType;

import com.github.reinert.jjschema.Attributes;

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
