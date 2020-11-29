package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;

public class FieldNumberYesOrNo extends FieldType {
    
    @Getter
    public enum OptionsNumberYesOrNo {
        No("0", "否"), Yes("1", "是");
        
        private final String value;
        
        private final String display;
        
        OptionsNumberYesOrNo(String value, String display) {
            
            this.value = value;
            
            this.display = display;
        }
    }
    
    @SuppressWarnings("serial")
    private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
        {
            for (OptionsNumberYesOrNo v : OptionsNumberYesOrNo.values()) {
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
