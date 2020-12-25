package com.weimob.webfwk.state.module.tenant;

import javax.sql.DataSource;

import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.sql.AbstractDao;

public class SystemTenantMetaDataSource extends AbstractDao {
    
    public SystemTenantMetaDataSource() {
        super();
    }
    
    public SystemTenantMetaDataSource(DataSource dataSource) {
        super(dataSource);
    }
    
    public SystemTenantMetaDataSource(String propertiesFile) throws Exception {
        super(propertiesFile);
    }
    
    public boolean inDebugMode() {
        return super.inDebugMode() || ContextUtil.inDebugMode();
    }
}
