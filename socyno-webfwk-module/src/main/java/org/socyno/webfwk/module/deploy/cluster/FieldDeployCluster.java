package org.socyno.webfwk.module.deploy.cluster;

import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 集群可选项类型定义
 *
 */
public class FieldDeployCluster extends FieldTableView {
    
    /**
     * 集群选项视图模型定义
     *
     */
    @Getter
    @Setter
    @ToString
    public static class OptionDeployCluster implements FieldOption {
        
        @Attributes(title = "编号")
        private long id;
        
        @Attributes(title = "所属环境", position = 10)
        private String environment;
        
        @Attributes(title = "集群类型", position = 30)
        private String type;
        
        @Attributes(title = "集群代码", position = 50)
        private String code;
        
        @Attributes(title = "集群名称", position = 60)
        private String title;
        
        @Override
        public String getOptionDisplay() {
            return String.format("%s/%s - %s", getType(), getCode(), getTitle());
        }
        
        @Override
        public String getOptionGroup() {
            try {
                return String.format("%s", ClassUtil.getSingltonInstance(FieldDeployEnvironment.class)
                        .queryDynamicValue(getEnvironment()).getDisplay());
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
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    private AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
     * SELECT
     *     c.id,
     *     c.code,
     *     c.title,
     *     c.environment
     * FROM
     *     system_deploy_cluster c
     */
    @Multiline
    private static final String SQL_QUERY_DEPLOY_CLUSTER = "X";
    
    /**
     * 获取可选部署集群列表
     * 
     */
    @Override
    public List<OptionDeployCluster> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        if (keyword == null || StringUtils.isBlank(keyword.getKeyword())) {
            return getDao().queryAsList(OptionDeployCluster.class,
                    String.format("%s WHERE c.state_form_status = 'enabled' ORDER BY c.environment, c.code, c.title",
                            SQL_QUERY_DEPLOY_CLUSTER));
        }
        
        return getDao().queryAsList(OptionDeployCluster.class, String.format(
                "%s WHERE c.state_form_status = 'enabled' AND (c.title LIKE CONCAT('%%', ?, '%%') OR c.code LIKE CONCAT('%%', ?, '%%'))"
                        + " ORDER BY c.environment, c.code, c.title",
                SQL_QUERY_DEPLOY_CLUSTER), new Object[] { keyword.getKeyword(), keyword.getKeyword() });
    }
}
