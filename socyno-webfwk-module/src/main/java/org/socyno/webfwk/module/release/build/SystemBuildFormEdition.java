package org.socyno.webfwk.module.release.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
@Attributes(title = "构建服务编辑")
public class SystemBuildFormEdition extends StateFormBasicForm {
    
    @Attributes(title = "名称", readonly = true)
    private String code;
    
    @Attributes(title = "类型", required = true, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "标题", required = true)
    private String title;
    
    @Attributes(title = "描述", required = true, type = FieldText.class)
    private String description;
}
