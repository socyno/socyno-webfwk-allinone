package org.socyno.webfwk.state.field;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OptionSystemBusiness implements FieldOption {
    
    @Attributes(title = "编号")
    private String id;
    
    @Attributes(title = "状态")
    private String state;
    
    @Attributes(title = "名称")
    private String name;
    
    @Override
    public String getOptionValue() {
        return getId();
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s:%s", getId(), getName());
    }
    
    @Override
    public void setOptionValue(String value) {
        setId(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj != null && OptionSystemBusiness.class.equals(obj.getClass())
                && this.getOptionValue().equals(((OptionSystemBusiness) obj).getOptionValue());
    }
    
    @Override
    public int hashCode() {
        return getOptionValue().hashCode();
    }
}
