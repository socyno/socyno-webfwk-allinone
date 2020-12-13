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
@Attributes(title = "系统功能查询")
public class SystemFeatureQueryDefault extends AbstractStateFormQuery {
    
    /**
     * SELECT f.* FROM system_feature f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_FEATURES = "X";
    
    /**
     * SELECT COUNT(1) FROM system_feature f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_FEATURES = "X";
    
    /** AND (
     *     f.code LIKE CONCAT('%', ?, '%')
     * OR
     *     f.name LIKE CONCAT('%', ?, '%')
     * OR
     *     f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_FEATURES_TMPL = "X";
    
    /** AND EXISTS (
     *     SELECT
     *          a.feature_id
     *     FROM
     *          system_feature_auth a
     *     WHERE
     *          a.feature_id = f.id
     *     AND
     *          a.auth_key LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_AUTHKEY_FEATURES_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "授权标识", position = 1020)
    private String authKey;
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuffer sqlstmt = new StringBuffer("WHERE 1 = 1");
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlstmt.append(" ").append(SQL_QUERY_NAMELIKE_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getAuthKey())) {
            sqlargs.add(getAuthKey());
            sqlstmt.append(" ").append(SQL_QUERY_AUTHKEY_FEATURES_TMPL);
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt.toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s", SQL_QUERY_COUNT_FEATURES, whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                            SQL_QUERY_ALL_FEATURES,
                            whereQuery.getSql(), getOffset(), getLimit()));
    }
}
