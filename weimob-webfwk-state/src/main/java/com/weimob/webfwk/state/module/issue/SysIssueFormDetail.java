package com.weimob.webfwk.state.module.issue;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.model.CommonFormAttachement;
import com.weimob.webfwk.util.state.field.FieldFormAttachements;

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
