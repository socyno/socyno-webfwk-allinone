package org.socyno.webfwk.module.systenant;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "创建租户信息")
public class SystemTenantFormCreation implements AbstractSystemTenant {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "租户代码", required = true)
    private String code;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
    
    @Attributes(title = "源码空间", description = "源代码存储的命名空间(如：socyno)。格式要求: 字母、数字、短横线，且不允许以数字开头。", required = true)
    private String codeNamespace;
    
    @Attributes(title = "程序组织", description = "程序及组件包存储的命名空间(如：org.socyno)。格式要求: 字母、数字、短横线、圆点，且不允许以圆点开头或结尾。", required = true)
    private String codeLibGroup;
}
