package com.weimob.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldTextLine;

@Getter
@Setter
@ToString
public class ApplicationFormUpgradeVersion extends StateFormBasicInput {

    @Attributes(title = "当前版本号", readonly = true, type = FieldTextLine.class)
    private String buildVersion;

    @Attributes(title = "修改版本号", required = true, type = FieldTextLine.class)
    private String upgradeVersion;

}
