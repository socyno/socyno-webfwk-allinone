package com.weimob.webfwk.state.field;

public class OptionSystemUsername extends OptionSystemUser {
    
    @Override
    public String getOptionValue() {
        return getUsername();
    }
    
    @Override
    public void setOptionValue(String value) {
        setUsername(value);
    }
}
