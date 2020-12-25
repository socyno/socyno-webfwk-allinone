package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;
import com.weimob.webfwk.state.model.CommonAttachementItem;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldFormAttachements;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用发布申请")
public class ReleaseMobileOnlineFormCreate extends StateFormBasicInput implements ReleaseMobileOnlineWithAttachements{

    @Attributes(title = "应用" , type = FieldReleaseMobileOnlineApplication.class , required = true , position = 1001)
    private String applicationName ;

    @Attributes(title = "商店" , type = FieldReleaseMobileOnlineAppStore.class , required = true , position = 1002)
    private FieldReleaseMobileOnlineAppStore.OptionStore[] store ;

    @Attributes(title = "版本", required = true, position = 1003)
    private String release ;

    @Attributes(title = "发布说明", required = true, position = 1004, type = FieldText.class)
    private String releaseNote ;

    @Attributes(title = "附件", position = 1005, type = FieldFormAttachements.class)
    private List<CommonAttachementItem> attachements;

    @Attributes(title = "额外审批人", position = 1006, type = FieldSystemUser.class)
    private OptionSystemUser specialApprover ;

}
