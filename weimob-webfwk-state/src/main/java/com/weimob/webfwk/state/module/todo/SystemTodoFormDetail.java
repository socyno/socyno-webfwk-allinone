package com.weimob.webfwk.state.module.todo;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项详情")
public class SystemTodoFormDetail extends SystemTodoFormDefault implements SystemTodoFormWithAssignees {
    
    @Attributes(title = "审批人清单", type = FieldSystemUser.class)
    private List<OptionSystemUser> assignees;
}
