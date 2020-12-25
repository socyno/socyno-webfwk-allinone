package com.weimob.webfwk.schmodel;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;

@Data
public class TenantBasicInfo {
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态")
    private String state;
    
    @Attributes(title = "租户代码")
    private String code;
    
    @Attributes(title = "租户名称")
    private String name;
}
