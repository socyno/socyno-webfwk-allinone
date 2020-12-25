package com.weimob.webfwk.state.module.option;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.adrianwalker.multilinestring.Multiline;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class DynamicFieldOptionQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字")
    private String keyword;
    
    /**
     * (
     *      s.class_path LIKE CONCAT('%', ?, '%')
     *   AND
     *      s.description LIKE CONCAT('%', ?, '%') 
     * )
     */
    @Multiline
    private final static String SQL_QUERY_KEYWORD = "X";
    
    public AbstractSqlStatement buildWhereSql() {
        
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        
        if (StringUtils.isNotBlank(keyword)) {
            sqlArgs.add(keyword);
            sqlArgs.add(keyword);
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(SQL_QUERY_KEYWORD);
        }
        
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlWhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT COUNT(1) FROM %s s %s",
                DynamicFieldOptionService.getInstance().getFormTable(), 
                query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT s.* FROM %s s %s ORDER BY s.class_path ASC LIMIT %s, %s",
                        DynamicFieldOptionService.getInstance().getFormTable(), 
                        query.getSql(), getOffset(), getLimit()));
    }
}
