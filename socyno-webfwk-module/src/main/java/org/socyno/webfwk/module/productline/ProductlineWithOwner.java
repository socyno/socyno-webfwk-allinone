package org.socyno.webfwk.module.productline;

import org.socyno.webfwk.state.field.OptionSystemUser;

public interface ProductlineWithOwner {
    
    public OptionSystemUser getOwner();
    
    public void setOwner(OptionSystemUser owner);
}
