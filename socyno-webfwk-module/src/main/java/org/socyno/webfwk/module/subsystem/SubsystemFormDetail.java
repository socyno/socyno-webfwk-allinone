package org.socyno.webfwk.module.subsystem;

import java.util.List;

import org.socyno.webfwk.module.application.FieldApplication;
import org.socyno.webfwk.module.application.FieldApplication.OptionApplication;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubsystemFormDetail extends SubsystemFormDefault implements SubsystemWithApplications {
    
    @Attributes(title = "应用清单", type = FieldApplication.class)
    private List<OptionApplication> applications;
    
}
