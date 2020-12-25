package com.weimob.webfwk.state.service;

import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.service.AbstractSimpleLockService;
import com.weimob.webfwk.util.sql.AbstractDao;

import lombok.Getter;

public class SimpleLockService extends AbstractSimpleLockService {
    
    @Override
    public AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Getter
    private final static AbstractSimpleLockService Instance = new SimpleLockService();
    
}
