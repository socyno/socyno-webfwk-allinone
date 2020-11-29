package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.github.reinert.jjschema.v1.FieldType.FieldOptionsType;

import lombok.Getter;

public class FieldStringYesOrNo extends FieldType {
    
    @Getter
    public enum OptionsStringYesOrNo {
        No("no", "否"), Yes("yes", "是");
        
        private final String value;
        
        private final String display;
        
        OptionsStringYesOrNo(String value, String display) {
            
            this.value = value;
            
            this.display = display;
        }
    }
    
    @SuppressWarnings("serial")
    private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
        {
            for (OptionsStringYesOrNo v : OptionsStringYesOrNo.values()) {
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
