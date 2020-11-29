package org.socyno.webfwk.state.field;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OptionSystemFeature implements FieldOption {
    
    @Attributes(title = "编号", position = 0)
    private long   id;
    
    @Attributes(title = "代码", position = 1)
    private String code;
    
    @Attributes(title = "名称", position = 2)
    private String name;
    
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
        return String.format("%s:%s", getCode(), getName());
    }
}
