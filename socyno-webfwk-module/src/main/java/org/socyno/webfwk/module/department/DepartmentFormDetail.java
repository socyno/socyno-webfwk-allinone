package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartmentFormDetail extends DepartmentFormDefault implements DepartmentWithSubsystems {
    
    @Attributes(title = "业务系统清单", type = FieldSubsystemAccessors.class)
    private List<SubsystemFormSimple> subsystems;
    
}
