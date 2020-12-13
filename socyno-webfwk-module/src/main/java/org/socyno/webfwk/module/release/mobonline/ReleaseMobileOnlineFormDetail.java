package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;

import lombok.*;

import java.util.List;

import org.socyno.webfwk.state.model.CommonAttachementItem;
import org.socyno.webfwk.util.state.field.FieldFormAttachements;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用发布详情")
public class ReleaseMobileOnlineFormDetail extends ReleaseMobileOnlineFormDefault implements ReleaseMobileOnlineWithAttachements{

    @Attributes(title = "附件",type = FieldFormAttachements.class)
    private List<CommonAttachementItem> attachements;
}
