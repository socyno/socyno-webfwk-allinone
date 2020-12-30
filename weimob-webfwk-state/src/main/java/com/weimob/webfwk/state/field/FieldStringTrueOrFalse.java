package com.weimob.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;

public class FieldStringTrueOrFalse extends FieldType {
    
    @Getter
    public enum OptionsStringTrueOrFalse {
        False("false", "否"), 
        True("true",   "是");
        
        private final String value;
        
        private final String display;
        
        OptionsStringTrueOrFalse(String value, String display) {
            
            this.value = value;
            
            this.display = display;
        }
    }
    
    @SuppressWarnings("serial")
    private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
        {
            for (OptionsStringTrueOrFalse v : OptionsStringTrueOrFalse.values()) {
                add(FieldSimpleOption.create(v.getValue(), v.getDisplay()));
            }
        }
    };
    
    @Override
    public List<FieldSimpleOption> getStaticOptions() {
        return Collections.unmodifiableList(options);
    }
    
    @Override
    public FieldType.FieldOptionsType getOptionsType() {
        return FieldOptionsType.STATIC;
    }
}
