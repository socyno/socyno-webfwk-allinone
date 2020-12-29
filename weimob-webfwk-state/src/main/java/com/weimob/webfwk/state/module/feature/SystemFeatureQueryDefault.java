package com.weimob.webfwk.state.module.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

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
    
    /** (
     *     f.code LIKE CONCAT('%', ?, '%')
     * OR
     *     f.name LIKE CONCAT('%', ?, '%')
     * OR
     *     f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_FEATURES_TMPL = "X";
    
    /** EXISTS (
     *     SELECT
     *          tf.feature_id
     *     FROM
     *          system_tenant_feature tf,
     *          system_tenant t
     *     WHERE
     *          tf.feature_id = f.id
     *     AND
     *          tf.tenant_id = t.id 
     *     AND
     *          t.code = ?
     * )
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES_TMPL = "X";
    
    /** EXISTS (
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
    
    @Attributes(title = "关键字")
    private String nameLike;
    
    @Attributes(title = "授权标识")
    private String authKey;
    
    @Attributes(title = "租户代码")
    private String tenantCode;
    
    @Attributes(title = "编号清单", description = "以提供的功能编号进行查询，多个可使用逗号、空格或分号进行分割")
    private String idsIn;
    
    @Attributes(title = "帐户清单", description = "以提供的功能代码进行查询，多个可使用逗号、空格或分号进行分割")
    private String codesIn;
    
    public SystemFeatureQueryDefault(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SystemFeatureQueryDefault() {
        this(null, null);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlstmt = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_NAMELIKE_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getAuthKey())) {
            sqlargs.add(getAuthKey());
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_AUTHKEY_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getTenantCode())) {
            sqlargs.add(getTenantCode());
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_TENANT_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] ids;
            if ((ids = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && ids.length > 0) {
                sqlargs.addAll(Arrays.asList(ids));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.id")
                        .append(CommonUtil.join("?", ids.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        if (StringUtils.isNotBlank(getCodesIn())) {
            String[] codes;
            if ((codes = CommonUtil.split(getCodesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && codes.length > 0) {
                sqlargs.addAll(Arrays.asList(codes));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.code")
                        .append(CommonUtil.join("?", codes.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, " WHERE ").toString());
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
