package com.weimob.webfwk.module.productline;

import com.weimob.webfwk.state.field.OptionSystemUser;

public interface ProductlineWithOwner {
    
    public OptionSystemUser getOwner();
    
    public void setOwner(OptionSystemUser owner);
}
