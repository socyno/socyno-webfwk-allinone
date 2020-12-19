package org.socyno.webfwk.module.sysaccess;

import org.socyno.webfwk.module.sysaccess.SystemAccessApplyFormSimple.AccessType;
import org.socyno.webfwk.state.field.FieldSystemRole;
import org.socyno.webfwk.state.field.OptionSystemBusiness;
import org.socyno.webfwk.state.field.OptionSystemRole;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;

@Data
public class SystemAccessApplyBusinessEntity {
    
    @Attributes(title = "申请单号")
    private Long applyId;
    
    @Attributes(title = "角色", required = true, type = FieldSystemRole.class)
    private OptionSystemRole role;
    
    @Attributes(title = "类型", required = true, type = SystemAccessApplyFormSimple.FieldOptionsAccessType.class)
    private String accessType;
    
    @Attributes(title = "业务", type = FieldSystemBusinessAccessApply.class)
    private OptionSystemBusiness business;
    
    public boolean isBusinessEntity() {
        return AccessType.BUSINESS.getValue().equalsIgnoreCase(accessType);
    }
    
}
