package org.socyno.webfwk.state.module.role;

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

@Getter
@Setter
@ToString
@Attributes(title = "系统角色查询")
public class SystemRoleListDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    public SystemRoleListDefaultQuery() {
        super();
    }
    
    public SystemRoleListDefaultQuery(long page, int limit) {
        this(null, page, limit);
    }
    
    public SystemRoleListDefaultQuery(String nameLike, long page, int limit) {
        this.nameLike = nameLike;
        this.setPage(page);
        this.setLimit(limit);
    }
    
    /**
     SELECT
        f.*
     FROM
        %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     SELECT
        COUNT(1)
     FROM
        %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     WHERE
         f.code LIKE CONCAT('%', ?, '%')
     OR
         f.name LIKE CONCAT('%', ?, '%')
     OR
         f.description LIKE CONCAT('%', ?, '%')
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
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
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT, SystemRoleService.DEFAULT.getFormTable()),
                        whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL, SystemRoleService.DEFAULT.getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
