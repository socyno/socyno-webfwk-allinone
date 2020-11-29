package org.socyno.webfwk.state.module.todo;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项关闭")
public class SystemTodoFormForClose extends BasicStateForm {
    
    @Attributes(title = "类型", readonly = true)
    private String category;
    
    @Attributes(title = "标题", readonly = true)
    private String title;
    
    @Attributes(title = "处理人", type = FieldSystemUser.class)
    private Long closedUserId;
    
    @Attributes(title = "处理结果", required = true)
    private String result;
}
