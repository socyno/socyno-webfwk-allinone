package org.socyno.webfwk.state.service;

import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.service.AbstractSimpleLockService;
import org.socyno.webfwk.util.sql.AbstractDao;

import lombok.Getter;

public class SimpleLockService extends AbstractSimpleLockService {
    
    @Override
    public AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Getter
    private final static AbstractSimpleLockService Instance = new SimpleLockService();
    
}
