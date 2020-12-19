package org.socyno.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldTextLine;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationFormUpgradeVersion extends StateFormBasicInput {

    @Attributes(title = "当前版本号", readonly = true, type = FieldTextLine.class)
    private String buildVersion;

    @Attributes(title = "修改版本号", required = true, type = FieldTextLine.class)
    private String upgradeVersion;

}
