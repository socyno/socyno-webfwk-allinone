package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.util.state.field.FieldDateOnly;

import lombok.Data;

@Data
public class OptionReleaseMobileOnlineAppStore {
    
    @Attributes(title = "主表ID")
    private Long releaseAppStoreId;
    
    @Attributes(title = "商店名")
    private String storeName;
    
    @Attributes(title = "状态", type = ReleaseMobileOnlineFormDetail.FieldOptionsAppStoreStatus.class)
    private String status;
    
    @Attributes(title = "备注")
    private String comment;
    
    @Attributes(title = "外发时间", type = FieldDateOnly.class)
    private String outerTime;
    
    @Attributes(title = "上架时间", type = FieldDateOnly.class)
    private String uploadTime;
    
    @Attributes(title = "渠道包")
    private String channelName;
}
