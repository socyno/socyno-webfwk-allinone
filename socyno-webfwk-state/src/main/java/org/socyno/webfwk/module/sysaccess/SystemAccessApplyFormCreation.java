package org.socyno.webfwk.module.sysaccess;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
public class SystemAccessApplyFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "申请原因", required = true, type = FieldText.class)
    private String reason;
    
    @Attributes(title = "业务及角色", type = FieldSystemAccessApplyBusinessEntity.class, required = true)
    private List<SystemAccessApplyBusinessEntity> businesses;
}
