package com.weimob.webfwk.state.util;

import com.weimob.webfwk.state.abs.AbstractStateAction;

public interface StateFormEventBaseEnum {
    
    public String name();
    
    public AbstractStateAction<?, ?, ?> getAction();
    
    public default String getName() {
        return name().replaceAll("([^A-Z])([A-Z])", "$1_$2").replaceAll("\\_+", "_").toLowerCase();
    }
}
