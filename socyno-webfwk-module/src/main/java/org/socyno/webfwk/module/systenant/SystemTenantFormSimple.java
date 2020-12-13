package org.socyno.webfwk.module.systenant;

import org.socyno.webfwk.module.systenant.SystemTenantFormDetail.FieldOptionsState;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户信息清单")
public class SystemTenantFormSimple implements AbstractSystemTenant {
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "版本")
    private Long revision;
    
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
