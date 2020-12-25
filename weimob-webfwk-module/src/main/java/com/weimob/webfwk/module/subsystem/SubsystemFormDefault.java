package com.weimob.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.productline.FieldProductline;
import com.weimob.webfwk.module.productline.FieldProductline.OptionProductline;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubsystemFormDefault extends SubsystemFormSimple implements SubsystemWithOwners, SubsystemWithProductlines , SubsystemWithAppSummary {
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private List<OptionSystemUser> owners;
    
    @Attributes(title = "产品线", type = FieldProductline.class)
    private List<OptionProductline> productlines;

    @Attributes(title = "业务系统部署机组概要")
    private SubsystemApplicationSummary appSummary;

}
