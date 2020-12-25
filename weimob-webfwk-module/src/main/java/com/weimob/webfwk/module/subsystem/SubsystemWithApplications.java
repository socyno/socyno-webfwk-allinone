package com.weimob.webfwk.module.subsystem;

import java.util.List;

import com.weimob.webfwk.module.application.FieldApplication.OptionApplication;

public interface SubsystemWithApplications extends SubsystemAbstractForm {
    
    public List<OptionApplication> getApplications();
    
    public void setApplications(List<OptionApplication> applications);
}
