package com.weimob.webfwk.state.module.tenant;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.exception.TenantDbInfoConnectException;
import com.weimob.webfwk.util.exception.TenantDbInfoMissingException;
import com.weimob.webfwk.util.exception.TenantDbInfoNotFoundException;
import com.weimob.webfwk.util.exception.TenantMissingException;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SystemTenantDataSource extends AbstractDao {
    
    protected abstract String getDbInfoName();
    
    private static final SystemTenantDataSource MainDataSource = new SystemTenantDataSource() {
        @Override
        protected String getDbInfoName() {
            return SystemTenantDbInfo.TYPES.main.name();
        }
    };
    
    public static AbstractDao getMain() {
        return MainDataSource;
    }
    
    private static final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    
    @Override
    public DataSource getDataSource() {
        String tenant;
        if (StringUtils.isBlank(tenant = SessionContext.getTenant())) {
            throw new TenantMissingException();
        }
        String dbInfoName;
        if (StringUtils.isBlank(dbInfoName = getDbInfoName())) {
            throw new TenantDbInfoMissingException(tenant, dbInfoName);
        }
        if (!tenantDataSources.containsKey(tenant)) {
            synchronized (this.getClass()) {
                if (!tenantDataSources.containsKey(tenant)) {
                    SystemTenantDbInfo sysTenantDb = null;
                    try {
                        if ((sysTenantDb = SystemTenantBasicService.getTenantDatabase(tenant, dbInfoName)) == null) {
                            throw new TenantDbInfoNotFoundException(tenant, dbInfoName);
                        }
                        tenantDataSources.put(tenant, getDbcp2DataSource(sysTenantDb));
                    } catch (MessageException e) {
                        throw e;
                    } catch (Throwable e) {
                        log.error(e.toString(), e);
                        throw new TenantDbInfoConnectException(tenant, dbInfoName);
                    }
                }
            }
        }
        return tenantDataSources.get(tenant);
    }
    
    private DataSource getDbcp2DataSource(SystemTenantDbInfo tenantDb) throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(tenantDb.getJdbcUrl());
        dataSource.setUsername(tenantDb.getJdbcUser());
        dataSource.setPassword(tenantDb.getJdbcToken());
        dataSource.setDriverClassName(tenantDb.getJdbcDriver());
        // 初始连接数量
        dataSource.setInitialSize(1);
        // 最小空闲连接数
        dataSource.setMinIdle(1);
        // 最大空闲连接数
        dataSource.setMaxIdle(5);
        // 最大活动连接数
        dataSource.setMaxTotal(10);
        // 最大连接等待时间（单位：毫秒）
        dataSource.setMaxWaitMillis(60000);
        
        // 确保连接信息正确
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return dataSource;
    }
}