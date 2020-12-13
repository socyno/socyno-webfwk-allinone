package org.socyno.webfwk.module.app.form;

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

    @Attributes(title = "当前版本号", readonly = true , position = 1010, type = FieldTextLine.class)
    private String buildMainVersion;

    @Attributes(title = "修改版本号", required = true, position = 1020, type = FieldTextLine.class)
    private String buildVersion;

}
