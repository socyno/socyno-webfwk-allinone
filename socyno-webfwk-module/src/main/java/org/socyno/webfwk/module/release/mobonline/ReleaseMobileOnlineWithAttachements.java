package org.socyno.webfwk.module.release.mobonline;

import java.util.List;

import org.socyno.webfwk.state.model.CommonAttachementItem;

public interface ReleaseMobileOnlineWithAttachements {
    public List<CommonAttachementItem> getAttachements();
    
    public void setAttachements(List<CommonAttachementItem> attachements);
}
