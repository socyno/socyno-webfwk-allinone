package org.socyno.webfwk.module.deploy.cluster;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import lombok.*;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.state.field.*;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

/**
 * 集群的租户命名空间字段类型定义，用于集群管理通用流程中注册租户的命名空间
 */
public class FieldDeployNamespace extends FieldTableView {

    /**
     * 集群下的租户命名空间列表视图模型定义
     */
    @Getter
    @Setter
    @ToString
    public static class OptionDeployClusterNamespace extends CreationDeployClusterNamespace
            implements FieldOption {

        @Attributes(title = "编号", position = 5)
        private Long id;

        @Attributes(title = "所属环境", position = 100)
        private String environment;

        @Attributes(title = "所属环境名称", position = 400)
        private String envDisplay;

        @Attributes(title = "租户代码", position = 200)
        private String tenantCode;

        @Attributes(title = "租户名称", position = 300)
        private String tenantName;

        @Attributes(title = "集群类型", position = 350)
        private String clusterType;

        @Attributes(title = "集群代码", position = 500)
        private String clusterCode;

        @Attributes(title = "集群名称", position = 600)
        private String clusterTitle;

        @Override
        public String getOptionDisplay() {
            return String.format("%s:%s/%s", getTenantCode(), getClusterTitle(), getNamespace());
        }

        @Override
        public String getOptionGroup() {
            try {
                return String.format("%s - %s", getEnvDisplay(), getClusterTitle());
            } catch (RuntimeException e) {
                throw (RuntimeException) e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getOptionValue() {
            return "" + getId();
        }

        @Override
        public void setOptionValue(String value) {
            setId(Long.valueOf(value));
        }
    }

    /**
     * 集群下的租户命名空间注册视图模型定义
     */
    @Getter
    @Setter
    @ToString
    public static class CreationDeployClusterNamespace {

        @Attributes(title = "集群", position = 400, required = true, type = FieldDeployCluster.class)
        private Long clusterId;

        @Attributes(title = "命名空间", position = 700, required = true)
        private String namespace;

        @Attributes(title = "描述信息", position = 800, type = FieldText.class)
        private String description;
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }

    protected static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }

    protected static AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    /**
     * 修改集群下注册的租户命名空间信息
     */
    public void setTenantDeployNamespace(long tenantId,
                                                @NonNull List<? extends OptionDeployClusterNamespace> namespaces) throws Exception {
        AbstractDao dao = getDao();
        dao.executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                dao.executeUpdate("UPDATE system_tenant_deploy_namespace SET deleted = 1 WHERE tenant_id = ?",
                        new Object[]{tenantId});
                for (OptionDeployClusterNamespace ns : namespaces) {
                    if (ns == null) {
                        continue;
                    }
                    if (ns.getId() != null) {
                        dao.executeUpdate(SqlQueryUtil.prepareUpdateQuery("system_tenant_deploy_namespace",
                                new ObjectMap().put("=id", ns.getId()).put("deleted", 0)));
                        continue;
                    }
                    if ((ns.getClusterId() == null || StringUtils.isBlank(ns.getNamespace()))) {
                        throw new MessageException(
                                String.format("添加租户部署命名空间失败，集群或命名空间必须提供（cluster_id = %s, namespace = %s）",
                                        ns.getClusterId(), ns.getNamespace()));
                    }
                    dao.executeUpdate(SqlQueryUtil.prepareInsertQuery("system_tenant_deploy_namespace",
                            new ObjectMap().put("tenant_id", tenantId).put("cluster_id", ns.getClusterId())
                                    .put("namespace", StringUtils.trimToEmpty(ns.getNamespace()))
                                    .put("description", StringUtils.trimToEmpty(ns.getDescription()))
                                    .put("deleted", 0)));
                }
                dao.executeUpdate("DELETE FROM system_tenant_deploy_namespace WHERE tenant_id = ? AND deleted = 1",
                        new Object[]{tenantId});
            }
        });
    }

    /**
     * SELECT
     *      n.id    AS id,
     *      t.code  AS tenantCode,
     *      t.name  AS tenantName,
     *      c.type  AS clusterType,
     *      c.code  AS clusterCode,
     *      c.title AS clusterTitle,
     *      e.display AS envDisplay,
     *      c.environment,
     *      n.namespace ,
     *      n.description
     * FROM
     *      system_tenant_deploy_namespace n,
     *      system_deploy_cluster c,
     *      system_deploy_environment e,
     *      system_tenant t
     * WHERE
     *      t.id = n.tenant_id
     * AND
     *      c.id = n.cluster_id
     * AND
     *      e.name=c.environment
     */
    @Multiline
    protected static final String SQL_QUERY_CLUSTER_NAMESPACE = "X";

    /**
     * 获取部署集群下注册的租户命名空间
     */
    public List<OptionDeployClusterNamespace> queryByClusterId(Long clusterId) throws Exception {
        if (clusterId == null) {
            return Collections.emptyList();
        }

        return getDao().queryAsList(OptionDeployClusterNamespace.class,
                String.format("%s AND n.cluster_id = %s ORDER BY c.environment, n.tenant_id, n.namespace",
                        SQL_QUERY_CLUSTER_NAMESPACE, clusterId));
    }

    /**
     * 获取租户的被授予的部署命名空间
     */
    public List<OptionDeployClusterNamespace> queryByTenantId(Long tenantId) throws Exception {
        if (tenantId == null) {
            return Collections.emptyList();
        }

        return getDao().queryAsList(OptionDeployClusterNamespace.class,
                String.format("%s AND n.tenant_id = %s ORDER BY c.environment, n.cluster_id, n.namespace",
                        SQL_QUERY_CLUSTER_NAMESPACE, tenantId));
    }

    /**
     * 获取当前租户应用可选部署命名空间清单
     */
    @Override
    public List<? extends OptionDeployClusterNamespace> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        String tenantCode;
        if (StringUtils.isBlank(tenantCode = SessionContext.getTenantOrNull())) {
            return Collections.emptyList();
        }
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        if (StringUtils.isBlank(keyword.getKeyword())) {
            return getDao().queryAsList(OptionDeployClusterNamespace.class,
                    String.format("%s AND t.code = ? ORDER BY c.environment, n.cluster_id, n.namespace",
                            SQL_QUERY_CLUSTER_NAMESPACE),
                    new Object[]{tenantCode});
        }

        return getDao().queryAsList(OptionDeployClusterNamespace.class, String.format(
                "%s AND t.code = ? AND (c.title LIKE CONCAT('%%', ?, '%%') OR c.code LIKE CONCAT('%%', ?, '%%')"
                        + " OR n.namespace LIKE CONCAT('%%', ?, '%%') OR c.environment LIKE CONCAT('%%', ?, '%%')) "
                        + " ORDER BY c.environment, n.cluster_id, n.namespace",
                SQL_QUERY_CLUSTER_NAMESPACE),
                new Object[]{tenantCode, keyword.getKeyword(), keyword.getKeyword(), keyword.getKeyword(), keyword.getKeyword()});
    }

    /**
     * 获取当前租户应用可选部署命名空间清单
     */
    @Override
    public List<? extends OptionDeployClusterNamespace> queryDynamicValues(Object[] namespaceIds) throws Exception {
        String tenantCode;
        if (StringUtils.isBlank(tenantCode = SessionContext.getTenantOrNull())) {
            return Collections.emptyList();
        }
        Long[] ids;
        if (namespaceIds == null || namespaceIds.length <= 0
                || (ids = ConvertUtil.asNonNullUniqueLongArray((Object[]) namespaceIds)).length <= 0) {
            return Collections.emptyList();
        }
        return getDao().queryAsList(OptionDeployClusterNamespace.class,
                String.format("%s AND t.code = ? AND n.id IN (%s) ORDER BY c.environment, n.cluster_id, n.namespace",
                        SQL_QUERY_CLUSTER_NAMESPACE, CommonUtil.join("?", ids.length, ",")),
                ArrayUtils.addAll(new Object[]{tenantCode}, (Object[]) ids));
    }
}
