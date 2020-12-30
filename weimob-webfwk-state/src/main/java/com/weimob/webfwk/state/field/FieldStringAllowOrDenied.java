package com.weimob.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;

public class FieldStringAllowOrDenied extends FieldType {
    
    @Getter
    public enum OptionsAllowOrDenied {
        Allowed("allowed",   "允许"), 
        Denied("denied",     "禁止");
        
        private final String value;
        
        private final String display;
        
        OptionsAllowOrDenied(String value, String display) {
            
            this.value = value;
            
            this.display = display;
        }
    }
    
    @SuppressWarnings("serial")
    private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
        {
            for (OptionsAllowOrDenied v : OptionsAllowOrDenied.values()) {
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
