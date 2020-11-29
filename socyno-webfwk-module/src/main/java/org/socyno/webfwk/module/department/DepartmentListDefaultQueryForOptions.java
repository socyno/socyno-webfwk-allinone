package org.socyno.webfwk.module.department;

import com.github.reinert.jjschema.Attributes;

@Attributes(title="可访问的业务系统清单查询，供通用下拉可选项使用")
public class DepartmentListDefaultQueryForOptions extends DepartmentListDefaultQuery {
    
    public DepartmentListDefaultQueryForOptions(String keyword) {
        super(keyword, 100, 1);
    }
    
    public DepartmentListDefaultQueryForOptions() {
        super(null, 100, 1);
    }
}
