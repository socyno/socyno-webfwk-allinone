package org.socyno.webfwk.state.module.feature;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.util.state.field.FieldText;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.module.feature.SystemFeatureFormSimple.FieldOptionsState;
import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统功能")
public class SystemFeatureFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
