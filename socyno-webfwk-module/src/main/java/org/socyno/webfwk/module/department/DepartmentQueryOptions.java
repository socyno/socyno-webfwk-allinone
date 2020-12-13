package org.socyno.webfwk.module.department;

import com.github.reinert.jjschema.Attributes;

@Attributes(title="可访问的业务系统清单查询，供通用下拉可选项使用")
public class DepartmentQueryOptions extends DepartmentQueryDefault {
    
    public DepartmentQueryOptions(String keyword) {
        super(keyword, 100, 1);
    }
    
    public DepartmentQueryOptions() {
        super(null, 100, 1);
    }
}
