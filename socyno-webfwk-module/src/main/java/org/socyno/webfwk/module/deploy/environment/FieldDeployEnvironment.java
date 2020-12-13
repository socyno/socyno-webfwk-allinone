package org.socyno.webfwk.module.deploy.environment;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.app.form.ApplicationService;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;

import java.util.Collections;
import java.util.List;

public class FieldDeployEnvironment extends FieldType {
    
    private AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    private AbstractDao getBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public FieldType.FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public static String getOptionDisplay(String name) throws Exception {
        List<OptionDeployEnvironment> environments = DeployEnvironmentService.getInstance()
                .allEnabled(OptionDeployEnvironment.class);
        String display = "";
        for (OptionDeployEnvironment environment : environments) {
            if (environment.getName().equals(name)) {
                display = environment.getDisplay();
                break;
            }
        }
        return display;
    }
    
    @Data
    public static class OptionDeployEnvironment implements AbstractDeployEnvironmentForm, FieldOption {
        
        @Attributes(title = "代码")
        private String name;
        
        @Attributes(title = "名称")
        private String display;
        
        @Override
        public void setOptionValue(String name) {
            setName(name);
        }
        
        @Override
        public String getOptionValue() {
            return getName();
        }
        
        @Override
        public String getOptionDisplay() {
            return getDisplay();
        }
    }
    
    /**
     * SELECT
     *     namespace_id
     * FROM
     *     application_namespace
     * WHERE
     *     application_id = ?
     */
    @Multiline
    private static final String SQL_QUERY_NAMESPACE_BY_KEYWORD = "X";
    
    /**
     * SELECT DISTINCT
     *     c.environment
     * FROM
     *     system_tenant_deploy_namespace n,
     *     system_deploy_cluster c
     * WHERE
     *     n.cluster_id = c.id
     * AND
     *     n.id in (%s)
     */
    @Multiline
    private static final String SQL_QUERY_ENVIRONMENT_BY_NAMESPACES = "X";
    
    public List<OptionDeployEnvironment> queryDynamicOptions(FilterBasicKeyword keyword) throws Exception {
        if (keyword != null && ApplicationService.getInstance().getFormName().equals(keyword.getFormName())
                && keyword.getFormId() != null) {
            List<Long> namespaceIds = getBaseDao().queryAsList(Long.class, SQL_QUERY_NAMESPACE_BY_KEYWORD,
                    new Object[] { keyword.getFormId() });
            if (namespaceIds == null || namespaceIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> environments = getDao().queryAsList(String.class,
                    String.format(SQL_QUERY_ENVIRONMENT_BY_NAMESPACES, CommonUtil.join("?", namespaceIds.size(), ",")),
                    namespaceIds.toArray());
            if (environments == null || environments.size() <= 0) {
                return Collections.emptyList();
            }
            return DeployEnvironmentService.getInstance().queryByNamesEnabled(OptionDeployEnvironment.class,
                    environments.toArray(new String[0]));
        }
        return DeployEnvironmentService.getInstance().allEnabled(OptionDeployEnvironment.class);
    }
    
    public List<OptionDeployEnvironment> queryDynamicOptionsByAppId(long applicationId) throws Exception {
        return queryDynamicOptions(
                new FilterBasicKeyword(null, ApplicationService.getInstance().getFormName(), applicationId));
    }
    
    public OptionDeployEnvironment queryDynamicValue(Object value) throws Exception {
        List<? extends FieldOption> envs;
        if ((envs = queryDynamicValues(new Object[] { value })).size() != 1) {
            return null;
        }
        return (OptionDeployEnvironment) envs.get(0);
    }
    
    public List<? extends FieldOption> queryDynamicValues(Object[] optionValues) throws Exception {
        String[] values;
        if ((values = ConvertUtil.asNonBlankUniqueTrimedStringArray(optionValues)).length <= 0) {
            return Collections.emptyList();
        }
        return DeployEnvironmentService.getInstance().queryByNames(OptionDeployEnvironment.class, true, values);
    }
    
}
