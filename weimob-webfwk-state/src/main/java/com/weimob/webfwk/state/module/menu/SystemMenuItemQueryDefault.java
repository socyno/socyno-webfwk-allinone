package com.weimob.webfwk.state.module.menu;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统菜单查询")
public class SystemMenuItemQueryDefault extends AbstractStateFormQuery {
    
    /**
     * SELECT
     *     m.*,
     *     d.id AS dir_id,
     *     d.name AS dir_name,
     *     p.id AS pane_id,
     *     p.name AS pane_name
     * FROM
     *     system_menu m,
     *     system_menu_dir d,
     *     system_menu_pane p
     * WHERE
     *     m.dir_id = d.id
     * AND 
     *     d.pane_id = p.id
     */
    @Multiline
    public static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT
     *     COUNT(1) 
     * FROM
     *     system_menu m,
     *     system_menu_dir d,
     *     system_menu_pane p
     * WHERE
     *     m.dir_id = d.id
     * AND 
     *     d.pane_id = p.id
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * AND (
     *         m.name LIKE CONCAT('%', ?, '%')
     *     OR
     *         d.name LIKE CONCAT('%', ?, '%')
     *     OR
     *         p.name LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    private AbstractSqlStatement buildWhereSql() {
        String sqlstmt = "";
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlstmt = SQL_QUERY_NAMELIKE_TMPL;
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt);
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
                .setSql(String.format("%s %s ORDER BY p.`order`, d.`order`, m.`order` LIMIT %s, %s",
                        SQL_QUERY_ALL, whereQuery.getSql(), getOffset(), getLimit()));
    }
}
