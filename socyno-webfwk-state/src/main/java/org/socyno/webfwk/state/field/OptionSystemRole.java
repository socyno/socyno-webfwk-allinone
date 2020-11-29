package org.socyno.webfwk.state.field;

import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OptionSystemRole implements FieldOption {
    
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
        return CommonUtil.ifBlank(getName(), getCode());
    }

    @Override
    public int hashCode() {
        if (getOptionValue() != null) {
            return getOptionValue().hashCode();
        }
        return Long.valueOf(getId()).hashCode();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof OptionSystemRole)) {
            return false;
        }
        OptionSystemRole anotherUser = (OptionSystemRole)another;
        if (getOptionValue() != null && getOptionValue().equals(anotherUser.getOptionValue())) {
            return true;
        }
        return getId() == anotherUser.getId();
    }
}
