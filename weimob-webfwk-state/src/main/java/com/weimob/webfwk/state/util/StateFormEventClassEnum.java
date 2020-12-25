package com.weimob.webfwk.state.util;

import com.weimob.webfwk.state.abs.AbstractStateAction;

public interface StateFormEventClassEnum {
    
    public String name();
    
    public Class<? extends AbstractStateAction<?, ?, ?>> getEventClass();
    
    public default String getName() {
        return name().replaceAll("([^A-Z])([A-Z])", "$1_$2").replaceAll("\\_+", "_").toLowerCase();
    }
}
