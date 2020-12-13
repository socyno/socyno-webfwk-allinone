package org.socyno.webfwk.module.deploy.environment;

import lombok.Getter;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.StringUtils;

@Getter
public enum EnvironmentProperties {
    
    DB_INFO_CONFIG_SVN_PATH("abc");
    
    private String key;
    
    EnvironmentProperties(String key) {
        this.key = key;
    }
    
    /**
     * SELECT
     *      property_value
     * FROM
     *      system_environment_properties sep
     * LEFT JOIN
     *      system_deploy_environment sde ON sep.env_id = sde.id
     * WHERE
     *      sep.property_key = ?
     * AND
     *      sde.name = ?
     * LIMIT 1
     */
    @Multiline
    private final static String SQL_QUERY_PROPERTIES_VALUE = "X";
    
    /**
     * 查询当前属性在指定环境的值
     * @param env
     * @param valueIfBlank
     * @return 不存在或为空白字串时，返回给定默认值
     * @throws Exception
     */
    public String getProperty(String env, String valueIfBlank) throws Exception {
        String propValue = getProperty(env);
        if (StringUtils.isBlank(propValue)) {
            return valueIfBlank;
        }
        return propValue;
    }
    
    /**
     * 查询当前属性在指定环境的值
     * @param env
     * @return 不存在则抛出异常
     * @throws Exception
     */
    public String getPropertyNotBlank(String env) throws Exception {
        String propValue = getProperty(env);
        if (StringUtils.isBlank(propValue)) {
            throw new MessageException(String.format("Eenvironment name '%s' or property name '%s' not found",
                    env, getKey()));
        }
        return propValue;
    }
    
    /**
     * 查询当前属性在指定环境的值
     * @param env
     * @return 不存在则返回 null
     * @throws Exception
     */
    private String getProperty(String env) throws Exception {
        return DeployEnvironmentService.getInstance().getFormBaseDao().queryAsObject(
                String.class, SQL_QUERY_PROPERTIES_VALUE,
                new Object[] { getKey(), env });
    }
}
