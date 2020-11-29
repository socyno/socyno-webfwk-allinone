package org.socyno.webfwk.module.department;

import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartmentFormForUpdate extends BasicStateForm {
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private OptionSystemUser owner;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessors.class)
    private List<SubsystemBasicForm> subsystems;
}
