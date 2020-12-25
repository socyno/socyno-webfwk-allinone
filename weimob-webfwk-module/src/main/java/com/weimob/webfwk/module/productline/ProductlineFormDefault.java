package com.weimob.webfwk.module.productline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormDefault extends ProductlineFormSimple implements ProductlineFormAbstract, ProductlineWithOwner {
    
    @Attributes(title = "负责人", type = FieldSystemUser.class)
    private OptionSystemUser owner;
}
