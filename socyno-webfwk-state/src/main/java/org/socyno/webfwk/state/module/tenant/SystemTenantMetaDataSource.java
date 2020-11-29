package org.socyno.webfwk.state.module.tenant;

import javax.sql.DataSource;

import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.sql.AbstractDao;

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
    
    @Override
    public int getColumnMapperCase() {
        return SQL_COLUMN_MAPPER_CASE_LOWER;
    }
}
