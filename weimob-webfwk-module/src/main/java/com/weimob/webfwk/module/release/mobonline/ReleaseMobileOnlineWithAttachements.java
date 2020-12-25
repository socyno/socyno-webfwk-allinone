package com.weimob.webfwk.module.release.mobonline;

import java.util.List;

import com.weimob.webfwk.state.model.CommonAttachementItem;

public interface ReleaseMobileOnlineWithAttachements {
    public List<CommonAttachementItem> getAttachements();
    
    public void setAttachements(List<CommonAttachementItem> attachements);
}
