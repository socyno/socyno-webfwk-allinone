package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.module.department.FieldDepartment;
import org.socyno.webfwk.module.department.FieldDepartment.OptionProductline;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;

@Getter
@Setter
@ToString
public class SubsystemFormDefault extends SubsystemFormSimple implements SubsystemWithOwners, SubsystemWithDepartments , SubsystemWithApplicationSummary {
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private List<OptionSystemUser> owners;
    
    @Attributes(title = "产品线", type = FieldDepartment.class)
    private List<OptionProductline> productlines;

    @Attributes(title = "业务系统部署机组概要")
    private SubsystemApplicationSummary appSummary;

}
