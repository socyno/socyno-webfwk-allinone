package org.socyno.webfwk.state.module.tenant;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantDbInfo.FieldOptionsDriver;
import org.socyno.webfwk.state.module.tenant.SystemTenantDbInfo.FieldOptionsName;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.NonNull;

public class SystemTenantBasicService {
    
    private static final String SuperTenantCode = "socyno.org";
    
    public static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
     * 确认给定的租户是否为超级租户
     */
    public static boolean equalsSuperTenant(String tenantCode) {
        return StringUtils.isNotBlank(tenantCode) && getSuperTenant().equals(tenantCode);
    }
    
    /**
     * 确认给定的租户是否为超级租户
     */
    public static String getSuperTenant() {
        return SuperTenantCode;
    }
    
    /**
     * 确认当前位于超级管理租户*代理*上下文
     */
    public static boolean inSuperTenantProxyContext() {
        if (StringUtils.isBlank(SessionContext.getProxyUsername())) {
            return false;
        }
        return equalsSuperTenant(AbstractUser.parseTenantFromUsername(SessionContext.getProxyUsername()));
    }
    
    /**
     * 确认当前位于超级管理租户上下文
     */
    public static boolean inSuperTenantContext() {
        return equalsSuperTenant(SessionContext.getTenantOrNull());
    }
    
    /**
         SELECT
             COUNT(1)
         FROM
             system_tenant t
         WHERE
             t.code = ?
         AND
             t.state_form_status = 'enabled'
         LIMIT 1
     */
    @Multiline
    private static final String SQL_CHECK_TENANT_ENABLED = "X";
    /**
     * 确认租户是否存在
     */
    public static boolean checkTenantEnabled(String tenantCode) throws Exception {
        return getDao().queryAsObject(Long.class, SQL_CHECK_TENANT_ENABLED,
                new Object[] { tenantCode }) > 0;
    }
    
    /**
         SELECT
             d.*
         FROM
             system_tenant_dbinfo d,
             system_tenant t
         WHERE
             t.id = d.tenant_id
         AND 
             t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_DBINFOS = "X";
    
    /**
     * 获取指定的数据库信息
     */
    public static SystemTenantDbInfo getTenantDatabase(String tenant, String dataName) throws Exception {
        if (StringUtils.isAnyBlank(tenant, dataName)) {
            return null;
        }
        List<SystemTenantDbInfo> result = getDao().queryAsList(SystemTenantDbInfo.class,
                String.format("%s AND d.name = ? ", SQL_QUERY_TENANT_DBINFOS), new Object[] { tenant, dataName });
        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }
    
    public static <T extends SystemTenantDbInfo> List<T> getTenantDatabases(@NonNull Class<T> clazz, String tenantCode)
            throws Exception {
        return getDao().queryAsList(clazz, SQL_QUERY_TENANT_DBINFOS, new Object[] { tenantCode });
    }
    
    public static void setTenantDatabases(final long tenantId, final List<SystemTenantDbInfo> databases) throws Exception {
        if (databases == null) {
            return;
        }
        Set<String> sameJdbcNames = new HashSet<>();
        for (SystemTenantDbInfo db : databases) {
            if (db == null) {
                continue;
            }
            boolean nameChecked = false;
            for (FieldSimpleOption option : ClassUtil.getSingltonInstance(FieldOptionsName.class).getStaticOptions()) {
                if (StringUtils.equals(option.getOptionValue(), db.getName())) {
                    nameChecked = true;
                    break;
                }
            }
            if (!nameChecked) {
                throw new MessageException(String.format("租户数据连接名称(%s)不支持！", db.getName()));
            }
            if (sameJdbcNames.contains(db.getName())) {
                throw new MessageException(String.format("租户数据连接条目存在重复(%s)！", db.getName()));
            }
            sameJdbcNames.add(db.getName());
            
            boolean driverChecked = false;
            for (FieldSimpleOption option : ClassUtil.getSingltonInstance(FieldOptionsDriver.class).getStaticOptions()) {
                if (StringUtils.equals(option.getOptionValue(), db.getJdbcDriver())) {
                    driverChecked = true;
                    break;
                }
            }
            if (!driverChecked) {
                throw new MessageException("租户数据连接驱动(%s)不支持！");
            }
            
            if (StringUtils.isBlank(db.getJdbcUrl())) {
                throw new MessageException("租户数据连接地址必须提供！");
            }
        }
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                getDao().executeUpdate("DELETE FROM system_tenant_dbinfo WHERE tenant_id = ?",
                        new Object[] { tenantId });
                for (SystemTenantDbInfo db : databases) {
                    if (db == null) {
                        continue;
                    }
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_tenant_dbinfo",
                        new ObjectMap().put("tenant_id", tenantId)
                                .put("name", db.getName())
                                .put("jdbc_url", db.getJdbcUrl())
                                .put("jdbc_driver", db.getJdbcDriver())
                                .put("jdbc_user", StringUtils.trimToEmpty(db.getJdbcUser()))
                                .put("jdbc_token", StringUtils.trimToEmpty(db.getJdbcToken()))
                    ));
                }
            }
        });
    }
    
    /**
         SELECT
             f.*
         FROM
             system_feature f,
             system_tenant_feature tf,
             system_tenant t
         WHERE
             t.id = tf.tenant_id
         AND 
             tf.feature_id = f.id
         AND 
             t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES = "X";
    
    public static <T extends OptionSystemFeature> List<T> getTenantFeatures(@NonNull Class<T> clazz, String tenantCode)
            throws Exception {
        return getDao().queryAsList(clazz, SQL_QUERY_TENANT_FEATURES, new Object[] { tenantCode });
    }
    
    public static void setTenantFeatures(final long tenantId, final List<OptionSystemFeature> features) throws Exception {
        if (features == null) {
            return;
        }
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                getDao().executeUpdate("DELETE FROM system_tenant_feature WHERE tenant_id = ?",
                        new Object[] { tenantId });
                for (OptionSystemFeature feature : features) {
                    if (feature == null) {
                        continue;
                    }
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_tenant_feature",
                        new ObjectMap().put("tenant_id", tenantId)
                                .put("=feature_id", feature.getOptionValue())
                    ));
                }
            }
        });
    }
}
