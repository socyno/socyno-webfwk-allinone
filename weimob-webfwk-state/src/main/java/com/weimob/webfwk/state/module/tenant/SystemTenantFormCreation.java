package com.weimob.webfwk.state.module.tenant;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "创建租户信息")
public class SystemTenantFormCreation extends StateFormBasicInput implements AbstractSystemTenantInput {
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "租户代码", required = true)
    private String code;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
    
    @Attributes(title = "源码空间", description = "源代码存储的命名空间(如：weimob)。格式要求: 字母、数字、短横线，且不允许以数字开头。", required = true)
    private String codeNamespace;
    
    @Attributes(title = "程序组织", description = "程序及组件包存储的命名空间(如：com.weimob)。格式要求: 字母、数字、短横线、圆点，且不允许以圆点开头或结尾。", required = true)
    private String codeLibGroup;
}
