package org.socyno.webfwk.state.module.todo;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项编辑")
public class SystemTodoFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "标题", required = true)
    private String title;
    
    @Attributes(title = "类型", required = true)
    private String category;
    
    @Attributes(title = "待办项标识", required = true)
    private String targetKey;
    
    @Attributes(title = "流程单标识", required = true)
    private String targetId;
    
    @Attributes(title = "待办项页面", required = true)
    private String targetPage;
    
    @Attributes(title = "流程发起人", type = FieldSystemUser.class)
    private OptionSystemUser applyUser;
    
    @Attributes(title = "审批人清单", required = true, type = FieldSystemUser.class)
    private List<OptionSystemUser> assignee;
    
}
