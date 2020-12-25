package com.weimob.webfwk.state.module.menu;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@Accessors(chain = true)
@Attributes(title = "菜单导航查询")
public class SystemMenuPaneQueryDefault extends AbstractStateFormQuery {
    
    /**
     * SELECT
     *     p.*
     * FROM
     *     system_menu_pane p
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT
     *     COUNT(1) 
     * FROM
     *     system_menu_pane p
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * p.name LIKE CONCAT('%', ?, '%')
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlstmt = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s", SQL_QUERY_COUNT, whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY p.`order` LIMIT %s, %s",
                        SQL_QUERY_ALL, whereQuery.getSql(), getOffset(), getLimit()));
    }
}
