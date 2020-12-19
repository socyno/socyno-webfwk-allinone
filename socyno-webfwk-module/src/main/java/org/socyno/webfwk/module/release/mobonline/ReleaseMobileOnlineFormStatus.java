package org.socyno.webfwk.module.release.mobonline;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldDateOnly;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用发布状态")
public class ReleaseMobileOnlineFormStatus extends StateFormBasicInput {
    
    @Attributes(title = "申请单号", position = 1001, readonly = true)
    private Long releaseAppStoreId;
    
    @Attributes(title = "商店名称", position = 1002, readonly = true)
    private String storeName;
    
    @Attributes(title = "当前状态", required = true, position = 1003, type = ReleaseMobileOnlineFormDetail.FieldOptionsAppStoreStatus.class)
    private String status;
    
    @Attributes(title = "备注", position = 1004, type = FieldText.class)
    private String comment;
    
    @Attributes(title = "外发时间", position = 1005, type = FieldDateOnly.class)
    private String outerTime;
    
    @Attributes(title = "上架时间", position = 1006, type = FieldDateOnly.class)
    private String uploadTime;
}
