package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.model.CommonAttachementItem;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldFormAttachements;
import org.socyno.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
public class ReleaseMobileOnlineFormUpload extends StateFormBasicInput implements ReleaseMobileOnlineWithAttachements {
    
    @Attributes(title = "发布说明", required = true, type = FieldText.class)
    private String releaseNote;
    
    @Attributes(title = "附件", type = FieldFormAttachements.class)
    private List<CommonAttachementItem> attachements;
    
}
