package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OptionSystemMenuDir implements FieldOption {
    
    @Attributes(title = "编号", position = 0)
    private long  id;
    
    @Attributes(title = "名称", position = 2)
    private String name;
    
    @Attributes(title = "面板", position = 3)
    private String pane;
    
    @Override
    public String getOptionValue() {
        return "" + getId();
    }
    
    @Override
    public void setOptionValue(String value) {
        setId(new Long(value));
    }
    
    @Override
    public String getOptionDisplay() {
        return getName();
    }
    
    @Override
    public String getOptionGroup() {
        return getPane();
    }
}
