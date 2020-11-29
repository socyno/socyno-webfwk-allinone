package org.socyno.webfwk.module.app.form;

import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationFormWithSubsystem extends ApplicationFormSimple implements ApplicationWithSubsystemEntity {
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessors.class)
    private SubsystemBasicForm subsystem;
    
}
