package org.socyno.webfwk.module.sysissue;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "分配系统故障或申请单", 
            visibleSelector = {"event_assign"},
            requiredSelector = {"event_assign"},
            editableSelector = {"event_assign"})
public class SysIssueFormForAssign extends SysIssueFormDetail {
    
}
