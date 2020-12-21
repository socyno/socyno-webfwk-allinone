package org.socyno.webfwk.state.module.issue;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "提交系统故障或需求申请单", 
            visibleSelector = {"event_create"},
            requiredSelector = {"event_create"},
            editableSelector = {"event_create"})
public class SysIssueFormCreation extends SysIssueFormDetail {
    
}
