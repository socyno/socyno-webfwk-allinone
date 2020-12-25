package com.weimob.webfwk.module.release.build;

import com.github.reinert.jjschema.v1.FieldOption;

public class SystemBuildFormOption extends SystemBuildFormSimple implements FieldOption {
    
    @Override
    public String getOptionValue() {
        return getCode();
    }
    
    @Override
    public void setOptionValue(String value) {
        setCode(value);
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s-%s", getCode(), getTitle());
    }
}
