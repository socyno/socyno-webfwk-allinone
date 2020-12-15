package org.socyno.webfwk.module.sysaccess;

import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.state.field.FieldSystemRole;
import org.socyno.webfwk.state.field.OptionSystemRole;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;

@Data
public class SystemAccessApplySubSystemEntity {
    
    @Attributes(title = "权限申请主表ID")
    private Long accessRequestId;
    
    @Attributes(title = "权限类型", type = SystemAccessApplyFormDetail.FieldOptionsAccessType.class, required = true, position = 1010)
    private String accessType;
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessApply.class, required = false, position = 1020)
    private SubsystemFormSimple subsystem;
    
    private Long subsystemId;
    
    @Attributes(title = "角色", required = true, position = 1030, type = FieldSystemRole.class)
    private OptionSystemRole role;
    private Long roleId;
    
}
