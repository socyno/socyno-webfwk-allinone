package org.socyno.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldTextLine;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationFormUpgradeVersion extends BasicStateForm {

    @Attributes(title = "当前版本号", readonly = true, type = FieldTextLine.class)
    private String buildVersion;

    @Attributes(title = "修改版本号", required = true, type = FieldTextLine.class)
    private String upgradeVersion;

}
