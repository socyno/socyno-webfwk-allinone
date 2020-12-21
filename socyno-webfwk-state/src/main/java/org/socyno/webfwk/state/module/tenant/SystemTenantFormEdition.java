package org.socyno.webfwk.state.module.tenant;

import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemFeature;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantFormDetail.FieldOptionDbInfo;
import org.socyno.webfwk.state.module.tenant.SystemTenantFormDetail.FieldOptionsState;
import org.socyno.webfwk.state.util.StateFormBasicInput;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑租户信息")
public class SystemTenantFormEdition extends StateFormBasicInput implements AbstractSystemTenantInput {
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "租户代码", readonly = true)
    private String code;
    
    @Attributes(title = "源码空间", readonly = true)
    private String codeNamespace;
    
    @Attributes(title = "程序组织", readonly = true)
    private String codeLibGroup;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
    
    @Attributes(title = "授权功能", type = FieldSystemFeature.class)
    private List<OptionSystemFeature> features;
    
    @Attributes(title = "数据连接", required = true, type = FieldOptionDbInfo.class)
    private List<SystemTenantDbInfo> databases;
}
