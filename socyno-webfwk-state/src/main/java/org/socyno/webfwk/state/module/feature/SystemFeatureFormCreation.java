package org.socyno.webfwk.state.module.feature;

import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.util.state.field.FieldText;
import org.socyno.webfwk.state.field.OptionSystemAuth;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加系统功能")
public class SystemFeatureFormCreation implements AbstractStateForm {
    @Attributes(title = "编号", readonly = true)
    private Long   id;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long   revision;
    
    @Attributes(title = "代码", required = true, position = 1010)
    private String code;
    
    @Attributes(title = "名称", required = true, position = 1020)
    private String name;
    
    @Attributes(title = "描述", position = 1030, type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", position = 1040, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
