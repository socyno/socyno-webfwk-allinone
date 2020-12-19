package org.socyno.webfwk.module.sysaccess;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
public class SystemAccessApplyFormCreation extends StateFormBasicForm  {

    @Attributes(title = "申请原因", required = true, position = 1001 ,type = FieldText.class)
    private String reasonForApplication ;

    @Attributes(title = "权限及角色" , type = FieldSystemAccessApplySubSystemEntity.class , required = true , position = 1003)
    private List<SystemAccessApplySubSystemEntity> subSystems ;
}
