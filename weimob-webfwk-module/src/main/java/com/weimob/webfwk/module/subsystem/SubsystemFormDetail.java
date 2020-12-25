package com.weimob.webfwk.module.subsystem;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.FieldApplication;
import com.weimob.webfwk.module.application.FieldApplication.OptionApplication;

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
