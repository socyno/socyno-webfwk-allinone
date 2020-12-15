package org.socyno.webfwk.state.module.feature;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.OptionSystemAuth;

import com.github.reinert.jjschema.Attributes;

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
