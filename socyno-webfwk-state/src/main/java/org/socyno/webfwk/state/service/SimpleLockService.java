package org.socyno.webfwk.state.service;

import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.service.AbstractSimpleLockService;
import org.socyno.webfwk.util.sql.AbstractDao;

public class SimpleLockService extends AbstractSimpleLockService {
    
    protected AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    public final static AbstractSimpleLockService DEFAULT = new SimpleLockService();
    
}
