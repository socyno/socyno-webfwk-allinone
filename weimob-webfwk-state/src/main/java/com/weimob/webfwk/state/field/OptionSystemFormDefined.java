package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OptionSystemFormDefined implements FieldOption {
    
    @Attributes(title = "编号", position = 10)
    private long   id;
    
    @Attributes(title = "代码", position = 20)
    private String formName;
    
    @Attributes(title = "名称", position = 30)
    private String formDisplay;
    
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
        return String.format("%s - %s", getFormName(), getFormDisplay());
    }
}
