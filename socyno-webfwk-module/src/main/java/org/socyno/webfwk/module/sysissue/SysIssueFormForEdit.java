package org.socyno.webfwk.module.sysissue;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "编辑系统故障或需求申请单", 
                visibleSelector = {"event_edit"},
                requiredSelector = {"event_edit"},
                editableSelector = {"event_edit"})
public class SysIssueFormForEdit extends SysIssueFormDetail {

}
