package org.socyno.webfwk.module.sysissue;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "关闭系统故障或需求申请单", 
        visibleSelector = {"event_close"},
        requiredSelector = {"event_close"},
        editableSelector = {"event_close"})
public class SysIssueFormClose extends SysIssueFormDetail {
    
}
