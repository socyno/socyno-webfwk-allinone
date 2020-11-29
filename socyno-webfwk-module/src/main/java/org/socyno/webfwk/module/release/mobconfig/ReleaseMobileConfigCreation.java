package org.socyno.webfwk.module.release.mobconfig;

import org.socyno.webfwk.module.release.mobconfig.ReleaseMobileConfigSimple.FieldOptionsAppStoreType;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用配置创建")
public class ReleaseMobileConfigCreation extends BasicStateForm {
    
    @Attributes(title = "应用名", required = true, position = 1001)
    private String applicationName;
    
    @Attributes(title = "系统类型", required = true, position = 1002, type = FieldOptionsAppStoreType.class)
    private String storeType;
    
    @Attributes(title = "审批人", required = true, position = 1003, type = FieldSystemUser.class)
    private OptionSystemUser approver;
    
}
