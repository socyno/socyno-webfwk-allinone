package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.model.CommonAttachementItem;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldFormAttachements;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ReleaseMobileOnlineFormUpload extends StateFormBasicInput implements ReleaseMobileOnlineWithAttachements {
    
    @Attributes(title = "发布说明", required = true, type = FieldText.class)
    private String releaseNote;
    
    @Attributes(title = "附件", type = FieldFormAttachements.class)
    private List<CommonAttachementItem> attachements;
    
}
