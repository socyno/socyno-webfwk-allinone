package org.socyno.webfwk.state.module.feature;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@Accessors(chain = true)
@Attributes(title = "租户功能查询")
public class SystemFeatureListTenantQuery extends AbstractStateFormQuery {
    
    /**
     * SELECT DISTINCT
     *     f.*
     * FROM
     *     system_feature f,
     *     system_tenant_feature tf,
     *     system_tenant t
     * WHERE
     *     f.id = tf.feature_id
     * AND
     *     t.id = tf.tenant_id
     * AND
     *     t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_ALL_TENANT = "X";

    /**
     * SELECT
     *     COUNT(DISTINCT f.id)
     * FROM
     *     system_feature f,
     *     system_tenant_feature tf,
     *     system_tenant t
     * WHERE
     *     f.id = tf.feature_id
     * AND
     *     t.id = tf.tenant_id
     * AND
     *     t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_TENANT = "X";
    
    /**
     * AND (
     *         f.code LIKE CONCAT('%', ?, '%')
     *     OR
     *         f.name LIKE CONCAT('%', ?, '%')
     *     OR
     *         f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TENANT_TMPL = "X";
    
    @Attributes(title = "租户", position = 1010)
    private String tenantCode;
    
    @Attributes(title = "关键字", position = 1020)
    private String nameLike;
    
    private AbstractSqlStatement buildWhereSql() {
        String sqlstmt = "";
        List<Object> sqlargs = new ArrayList<>();
        sqlargs.add(getTenantCode());
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlstmt = SQL_QUERY_NAMELIKE_TENANT_TMPL;
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt);
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s", SQL_QUERY_COUNT_TENANT, whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        SQL_QUERY_ALL_TENANT,
                            whereQuery.getSql(), getOffset(), getLimit()));
    }
}
