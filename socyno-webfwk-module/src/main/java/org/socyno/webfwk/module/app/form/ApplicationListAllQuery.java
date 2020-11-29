package org.socyno.webfwk.module.app.form;

/**
 * 默认情况下只显示当前的可见的应用清单。 如在部分场景必须跳过该限制时，可使用该查询设置, 请谨慎使用！！！
 * 
 */
public class ApplicationListAllQuery extends ApplicationListDefaultQuery {
    
    public ApplicationListAllQuery() {
        super();
    }
    
    public ApplicationListAllQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    @Override
    public boolean onlyMyVisibles() {
        return false;
    }
}
