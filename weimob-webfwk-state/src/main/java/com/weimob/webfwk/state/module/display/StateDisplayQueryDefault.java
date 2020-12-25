package com.weimob.webfwk.state.module.display;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
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
@Attributes(title = "表单显示配置列表查询")
public class StateDisplayQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字")
    private String keyword;
    
    /**
     * (
     *     a.name LIKE CONCAT('%',?,'%')
     * OR
     *     a.display LIKE CONCAT('%',?,'%')
     * OR
     *     a.remark LIKE CONCAT('%',?,'%')
     * )
     **/
    @Multiline
    public final static String SQL_QUERY_BY_KEYWORD = "X";
    
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
                StateDisplayService.getInstance().getFormTable(), 
                query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT a.* FROM %s a %s ORDER BY a.id DESC LIMIT %s, %s",
                        StateDisplayService.getInstance().getFormTable(), 
                        query.getSql(), getOffset(), getLimit()));
    }
}
