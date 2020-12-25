package com.weimob.webfwk.state.module.role;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemFeatureWithTenant;
import com.weimob.webfwk.state.field.OptionSystemFeature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统角色详情")
public class SystemRoleFormDetail extends SystemRoleFormDefault implements SystemRoleWithFeatures {
    @Attributes(title = "授权的功能", type = FieldSystemFeatureWithTenant.class)
    private List<OptionSystemFeature> features;
}
