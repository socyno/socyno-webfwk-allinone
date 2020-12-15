package org.socyno.webfwk.module.application;

/**
 * 默认情况下只显示当前的可见的应用清单。 如在部分场景必须跳过该限制时，可使用该查询设置, 请谨慎使用！！！
 * 
 */
public class ApplicationQueryAll extends ApplicationQueryDefault {
    
    public ApplicationQueryAll() {
        super();
    }
    
    public ApplicationQueryAll(Integer limit, Long page) {
        super(limit, page);
    }
    
    @Override
    public boolean onlyMyVisibles() {
        return false;
    }
}
