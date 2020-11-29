package org.socyno.webfwk.module.release.mobonline;

import org.socyno.webfwk.util.state.field.FieldDateOnly;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;

@Data
public class OptionReleaseMobileOnlineAppStore {
    
    @Attributes(title = "主表ID")
    private Long releaseAppStoreId;
    
    @Attributes(title = "商店名")
    private String storeName;
    
    @Attributes(title = "状态", type = ReleaseMobileOnlineDetailFrom.FieldOptionsAppStoreStatus.class)
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
