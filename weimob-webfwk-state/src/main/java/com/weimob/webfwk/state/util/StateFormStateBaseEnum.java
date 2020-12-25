package com.weimob.webfwk.state.util;

import com.github.reinert.jjschema.v1.FieldOption;
import com.weimob.webfwk.util.tool.StringUtils;

public interface StateFormStateBaseEnum extends FieldOption {
    public String getCode();
    
    public String getName();
    
    @Override
    public default String getOptionValue() {
        return getCode();
    }
    
    @Override
    public default void setOptionValue(String var1) {
        
    }
    
    @Override
    public default String getOptionDisplay() {
        String name = getClass().getName().concat(":").concat(getCode());
        String display = StateFormDisplayScheduled.getDisplay(name);
        return StringUtils.isNotBlank(display) ? display : getName();
    }
}
