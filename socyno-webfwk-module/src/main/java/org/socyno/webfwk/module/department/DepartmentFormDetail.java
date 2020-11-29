package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartmentFormDetail extends DepartmentListDefaultForm implements DepartmentWithSubsystems {
    
    @Attributes(title = "业务系统清单", type = FieldSubsystemAccessors.class)
    private List<SubsystemBasicForm> subsystems;
    
}
