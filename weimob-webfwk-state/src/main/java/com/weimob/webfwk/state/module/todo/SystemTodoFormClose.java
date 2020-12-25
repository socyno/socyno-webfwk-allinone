package com.weimob.webfwk.state.module.todo;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项关闭")
public class SystemTodoFormClose extends StateFormBasicInput {
    
    @Attributes(title = "类型", readonly = true)
    private String category;
    
    @Attributes(title = "标题", readonly = true)
    private String title;
    
    @Attributes(title = "处理人", type = FieldSystemUser.class)
    private Long closedUserId;
    
    @Attributes(title = "处理结果", required = true)
    private String result;
}
