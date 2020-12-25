package com.weimob.webfwk.state.module.tenant;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.module.tenant.SystemTenantFormDetail.FieldOptionsState;
import com.weimob.webfwk.state.util.StateFormBasicSaved;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户信息清单")
public class SystemTenantFormSimple extends StateFormBasicSaved implements AbstractSystemTenant {
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "租户代码")
    private String code;
    
    @Attributes(title = "租户名称")
    private String name;
    
    @Attributes(title = "源码空间")
    private String codeNamespace;
    
    @Attributes(title = "程序组织")
    private String codeLibGroup;
}
