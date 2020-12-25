package com.weimob.webfwk.state.module.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemAuths;
import com.weimob.webfwk.state.field.OptionSystemAuth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统功能详情")
public class SystemFeatureFormDetail extends SystemFeatureFormDefault implements SystemFeatureWithAuths {
    @Attributes(title = "接口/操作", type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
