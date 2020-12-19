package org.socyno.webfwk.module.productline;

import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemNoAnyLimited;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "代码", readonly = true)
    private String code;
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private OptionSystemUser owner;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "业务系统", type = FieldSubsystemNoAnyLimited.class)
    private List<SubsystemFormSimple> subsystems;
}
