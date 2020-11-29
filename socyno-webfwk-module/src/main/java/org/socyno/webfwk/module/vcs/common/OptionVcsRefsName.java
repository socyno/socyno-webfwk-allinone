package org.socyno.webfwk.module.vcs.common;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Data
@Accessors(chain = true)
public class OptionVcsRefsName implements FieldOption {
    
    @Attributes(title = "类型")
    private String type;
    
    @Attributes(title = "名称")
    private String name;
    
    @Override
    public String getOptionValue() {
        return getName();
    }
    
    @Override
    public void setOptionValue(String value) {
        setName(value);
    }
    
    @Override
    public String getOptionDisplay() {
        return getName();
    }
    
    @Override
    public String getOptionGroup() {
        return getType();
    }
    
    public OptionVcsRefsName() {
        
    }
    
    public OptionVcsRefsName(@NonNull VcsRefsType type, String name) {
        setName(name);
        setType(type.getDisplay());
    }
}
