package com.weimob.webfwk.state.module.config;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.field.FieldStringAllowOrDenied;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统参数配置列表查询")
public class SystemConfigQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字")
    private String keyword;
    
    @Attributes(title = "外部访问", type = FieldStringAllowOrDenied.class)
    private String external;
    
    /**
     * (
     *     a.name LIKE CONCAT('%',?,'%')
     *  OR
     *     a.value LIKE CONCAT('%',?,'%')
     *  OR
     *     a.comment LIKE CONCAT('%',?,'%')
     * )
     */
    @Multiline
    private final static String SQL_QUERY_BY_KEYWORD = "X";
    
    public SystemConfigQueryDefault() {
        super();
    }
    
    public SystemConfigQueryDefault(Integer limit) {
        super(limit);
    }
    
    public SystemConfigQueryDefault(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        
        if (StringUtils.isNotBlank(external)) {
            sqlargs.add(external);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.external = ?");
        }
        if (StringUtils.isNotBlank(keyword)) {
            sqlargs.add(keyword);
            sqlargs.add(keyword);
            sqlargs.add(keyword);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_BY_KEYWORD);
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT COUNT(1) FROM %s a %s",
                SystemConfigService.getInstance().getFormTable(),
                query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT a.* FROM %s a %s ORDER BY a.id DESC LIMIT %s, %s",
                        SystemConfigService.getInstance().getFormTable(), 
                        query.getSql(), getOffset(), getLimit()));
    }
}
