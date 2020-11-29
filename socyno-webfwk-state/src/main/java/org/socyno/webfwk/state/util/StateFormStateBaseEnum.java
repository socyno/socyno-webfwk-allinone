package org.socyno.webfwk.state.util;

import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOption;

public interface StateFormStateBaseEnum extends FieldOption {
    public String getCode();
    
    public String getName();
    
    @Override
    public default String getOptionValue() {
        return getCode();
    }
    
    @Override
    public default void setOptionValue(String var1){

    }
    
    @Override
    public default String getOptionDisplay() {
        String name = getClass().getName().concat(":").concat(getCode());
        String display = StateFormDisplayScheduled.getDisplay(name);
        return StringUtils.isNotBlank(display) ? display : getName();
    }
}
