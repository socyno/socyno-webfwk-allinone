package org.socyno.webfwk.module.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace.OptionDeployClusterNamespace;
import org.socyno.webfwk.util.exception.AbstractMethodUnimplimentedException;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;

public class FieldApplicationNamespace extends FieldTableView {
    
    @Data
    public static class OptionApplicationNamespace implements FieldOption {

        @Attributes(title = "应用编号")
        private long applicationId;

        @Attributes(title = "命名空间")
        private long namespaceId;

        @Attributes(title = "应用名称")
        private String applicationName;

        @Attributes(title = "命名空间")
        private String namespaceName;

        @Attributes(title = "集群类型")
        private String clusterType;

        @Attributes(title = "集群名称")
        private String clusterName;

        @Attributes(title = "环境")
        private String environment;

        @Attributes(title = "环境名称")
        private String envDisplay;

        @Attributes(title = "节点数")
        private Integer replicas;

        @Override
        public String getOptionDisplay() {
            return String.format("%s/%s/%s/%s/%s", getEnvironment(), getClusterName(), getNamespaceName(),
                    getApplicationName(), getReplicas());
        }

        @Override
        public String getOptionGroup() {
            return String.format("%s/%s/%s", getEnvironment(), getClusterName(), getNamespaceName());
        }

        @Override
        public String getOptionValue() {
            return String.format("%s/%s", getApplicationId(), getNamespaceId());
        }

        @Override
        public void setOptionValue(String value) {
            throw new AbstractMethodUnimplimentedException();
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * SELECT DISTINCT
     *      n.namespace_id,
     *      n.application_id,
     *      a.name AS applicationName,
     *      n.replicas
     * FROM
     *      application_namespace n
     * INNER JOIN
     *      application a ON a.id = n.application_id
     * WHERE
     *      n.application_id IN (%s)
     */
    @Multiline
    private final static String SQL_QUERY_QUERY_APPLICATION_NAMESPACE = "X";
    
    /**
     * 检索应用下的所有部署命名空间
     */
    public static List<OptionApplicationNamespace> queryByApplications(Long... applicationIds) throws Exception {
        if (applicationIds == null || applicationIds.length <= 0) {
            return Collections.emptyList();
        }
        List<OptionApplicationNamespace> namespaces = ApplicationService.getInstance().getFormBaseDao().queryAsList(
                OptionApplicationNamespace.class,
                String.format(SQL_QUERY_QUERY_APPLICATION_NAMESPACE, CommonUtil.join("?", applicationIds.length, ",")),
                applicationIds);
        if (namespaces == null || namespaces.isEmpty()) {
            return Collections.emptyList();
        }
        /**
         * 补全部署命名空间中的名称，集群，环境等信息
         */
        List<OptionApplicationNamespace> singleNamespaceItems;
        Map<Long, List<OptionApplicationNamespace>> mappedNamespaceItems = new HashMap<>();
        for (OptionApplicationNamespace oan : namespaces) {
            if ((singleNamespaceItems = mappedNamespaceItems.get(oan.getNamespaceId())) == null) {
                mappedNamespaceItems.put(oan.getNamespaceId(), singleNamespaceItems = new ArrayList<>());
            }
            singleNamespaceItems.add(oan);
        }
        for (OptionDeployClusterNamespace kcn : ClassUtil.getSingltonInstance(FieldDeployNamespace.class)
                .queryDynamicValues(mappedNamespaceItems.keySet().toArray())) {
            for (OptionApplicationNamespace oan : mappedNamespaceItems.get(kcn.getId())) {
                oan.setClusterType(kcn.getClusterType());
                oan.setNamespaceName(kcn.getNamespace());
                oan.setClusterName(kcn.getClusterTitle());
                oan.setEnvironment(kcn.getEnvironment());
                oan.setEnvDisplay(kcn.getEnvDisplay());
            }
        }
        return namespaces;
    }
    
    @Data
    public static class SimpleApplicationNamespace {
        private long applicaitonId;
        private long namespaceId;
        private int replicas;
    }
    /**
     * SELECT DISTINCT
     *      n.namespace_id,
     *      n.application_id,
     *      n.replicas
     * FROM
     *      application_namespace n
     */
    @Multiline
    private final static String SQL_QUERY_SIMPLE_APPLICATION_NAMESPACE = "X";

    public static List<SimpleApplicationNamespace> querySimpleByApplications(Long... applicationIds) throws Exception {
        if (applicationIds == null || applicationIds.length <= 0) {
            return Collections.emptyList();
        }
        return ApplicationService.getInstance().getFormBaseDao().queryAsList(
                SimpleApplicationNamespace.class,
                String.format("%s WHERE %s",SQL_QUERY_SIMPLE_APPLICATION_NAMESPACE,CommonUtil.join("application_id = ?", applicationIds.length, " OR ")),
                applicationIds);
    }

    /**
     * SELECT
     *      application_id
     * FROM
     *      application_namespace
     * WHERE
     *      application_id = ?
     * AND
     *      namespace_id = ?
     */
    @Multiline
    private final static String SQL_QUERY_CHECK_APPLICATION_NAMESPACE = "X";

    /**
     * 确认应用是否部署在指定的命名空间中
     */
    public static boolean check(long applicationId, long namespaceId) throws Exception {
        return ApplicationService.getInstance().getFormBaseDao().queryAsObject(Long.class,
                SQL_QUERY_CHECK_APPLICATION_NAMESPACE, new Object[]{applicationId, namespaceId}) != null;
    }

    /**
     * SELECT
     *      n.replicas
     * FROM
     *      application_namespace n
     * WHERE
     *      n.application_id = ?
     * AND
     *      n.namespace_id = ?
     */
    @Multiline
    private final static String SQL_QUERY_REPLICAS_APPLICATION_NAMESPACE = "X";

    public static Long getReplicas(long applicationId, long namespaceId) throws Exception {
        return ApplicationService.getInstance().getFormBaseDao().queryAsObject(Long.class,
                SQL_QUERY_REPLICAS_APPLICATION_NAMESPACE, new Object[]{applicationId, namespaceId});
    }


}
