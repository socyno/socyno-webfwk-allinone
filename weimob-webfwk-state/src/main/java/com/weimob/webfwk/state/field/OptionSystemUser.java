package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.weimob.webfwk.util.model.AbstractUser;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OptionSystemUser implements AbstractUser, FieldOption {
    
    @Attributes(title = "编号", position = 0)
    private Long   id;
    
    @Attributes(title = "代码", position = 1)
    private String username;
    
    @Attributes(title = "名称", position = 2)
    private String display;
    
    @Attributes(title = "邮箱")
    private String mailAddress;
    
    @Attributes(title = "状态")
    private String state;
    
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
        return CommonUtil.ifBlank(getDisplay(), getUsername());
    }
    
    @Override
    public int hashCode() {
        if (getOptionValue() != null) {
            return getOptionValue().hashCode();
        }
        if (getId() != null) {
            return getId().hashCode();
        }
        if (getUsername() != null) {
            return getUsername().hashCode();
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof OptionSystemUser)) {
            return false;
        }
        OptionSystemUser anotherUser = (OptionSystemUser)another;
        if (getOptionValue() != null && getOptionValue().equals(anotherUser.getOptionValue())) {
            return true;
        }
        if (getId() != null && getId().equals(anotherUser.getId())) {
            return true;
        }
        if (getUsername() != null && getUsername().equals(anotherUser.getUsername())) {
            return true;
        }
        return false;
    }
}
