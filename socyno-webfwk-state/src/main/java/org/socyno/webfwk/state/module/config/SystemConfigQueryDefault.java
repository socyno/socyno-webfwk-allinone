package org.socyno.webfwk.state.module.config;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

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
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        
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
