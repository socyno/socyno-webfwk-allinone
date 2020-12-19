package org.socyno.webfwk.state.module.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统角色查询")
public class SystemRoleQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字")
    private String nameLike;
    
    @Attributes(title = "角色编号", description = "以提供的角色编号进行查询，多个可使用逗号、空格或分号进行分割")
    private String idsIn;
    
    @Attributes(title = "角色代码", description = "以提供的角色代码进行查询，多个可使用逗号、空格或分号进行分割")
    private String codesIn;
    
    public SystemRoleQueryDefault() {
        super();
    }
    
    public SystemRoleQueryDefault(long page, int limit) {
        this(null, page, limit);
    }
    
    public SystemRoleQueryDefault(String nameLike, long page, int limit) {
        this.nameLike = nameLike;
        this.setPage(page);
        this.setLimit(limit);
    }
    
    /**
     * SELECT f.* FROM  %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * (
     *       f.code LIKE CONCAT('%', ?, '%')
     *   OR
     *       f.name LIKE CONCAT('%', ?, '%')
     *   OR
     *       f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlstmt = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] wordsIn;
            if ((wordsIn = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && wordsIn.length > 0) {
                sqlargs.addAll(Arrays.asList(wordsIn));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.id")
                        .append(CommonUtil.join("?", wordsIn.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        
        if (StringUtils.isNotBlank(getCodesIn())) {
            String[] wordsIn;
            if ((wordsIn = CommonUtil.split(getCodesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && wordsIn.length > 0) {
                sqlargs.addAll(Arrays.asList(wordsIn));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.code")
                        .append(CommonUtil.join("?", wordsIn.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                String.format(SQL_QUERY_COUNT, SystemRoleService.getInstance().getFormTable()), whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL, SystemRoleService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
