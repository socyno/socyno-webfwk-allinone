package org.socyno.webfwk.module.productline;

import com.github.reinert.jjschema.Attributes;

@Attributes(title="可访问的业务系统清单查询，供通用下拉可选项使用")
public class ProductlineQueryOptions extends ProductlineQueryDefault {
    
    public ProductlineQueryOptions(String keyword) {
        super(keyword, 100, 1L);
    }
    
    public ProductlineQueryOptions() {
        super(null, 100, 1L);
    }
}
