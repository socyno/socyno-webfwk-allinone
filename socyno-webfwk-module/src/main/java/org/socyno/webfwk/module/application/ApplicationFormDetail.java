package org.socyno.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

@Getter
@Setter
@ToString
public class ApplicationFormDetail extends ApplicationFormDefault
        implements ApplicationWithSubsystemEntity, ApplicationAbstractForm {
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessors.class)
    private SubsystemFormSimple subsystem;
}
