package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.model.CommonAttachementItem;
import com.weimob.webfwk.util.state.field.FieldFormAttachements;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用发布详情")
public class ReleaseMobileOnlineFormDetail extends ReleaseMobileOnlineFormDefault implements ReleaseMobileOnlineWithAttachements{

    @Attributes(title = "附件",type = FieldFormAttachements.class)
    private List<CommonAttachementItem> attachements;
}
