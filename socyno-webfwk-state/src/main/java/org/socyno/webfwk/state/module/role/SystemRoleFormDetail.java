package org.socyno.webfwk.state.module.role;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemFeatureWithTenant;
import org.socyno.webfwk.state.field.OptionSystemFeature;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统角色详情")
public class SystemRoleFormDetail extends SystemRoleFormDefault implements SubsystemRoleWithFeatures {
    @Attributes(title = "授权的功能", type = FieldSystemFeatureWithTenant.class)
    private List<OptionSystemFeature> features;
}
