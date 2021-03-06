package org.socyno.webfwk.state.module.issue;

import org.socyno.webfwk.state.model.CommonFormAttachement;
import org.socyno.webfwk.util.state.field.FieldFormAttachements;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Attributes(title = "系统故障或需求申请单详情")
public class SysIssueFormDetail extends SysIssueFormDefault {
    
    @Attributes(title = "附件", type = FieldFormAttachements.class,
            visibleTags= {"event_create", "event_edit"},
            editableTags= {"event_create", "event_edit"})
    private CommonFormAttachement[] attachments;
    
}
