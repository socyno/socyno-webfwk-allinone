package org.socyno.webfwk.module.department;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartmentListDefaultForm extends DepartmentBasicForm implements DepartmentAbstractForm, DepartmentWithOwner {
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private OptionSystemUser owner;
}
