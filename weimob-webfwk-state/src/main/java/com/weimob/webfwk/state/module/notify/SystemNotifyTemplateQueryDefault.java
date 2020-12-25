package com.weimob.webfwk.state.module.notify;

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

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "通知模板查询")
public class SystemNotifyTemplateQueryDefault extends AbstractStateFormQuery {
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     (
         f.code LIKE CONCAT('%', ?, '%')
       OR
         f.comment LIKE CONCAT('%', ?, '%')
       OR
         f.mail_to LIKE CONCAT('%', ?, '%')
       OR
         f.mail_cc LIKE CONCAT('%', ?, '%')
       OR
         f.mail_content LIKE CONCAT('%', ?, '%')
       OR
         f.message_to LIKE CONCAT('%', ?, '%')
       OR
         f.message_content LIKE CONCAT('%', ?, '%')
     )
    */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "是否包括已禁用", position = 1020)
    private boolean disableIncluded = false;
    
    @Attributes(title = "准确代码", position = 1030)
    private String codeEquals;
    
    public SystemNotifyTemplateQueryDefault(long page, int limit) {
        this(null, page, limit);
    }
    
    public SystemNotifyTemplateQueryDefault(String nameLike, long page, int limit) {
        this.nameLike = nameLike;
        this.setPage(page);
        this.setLimit(limit);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        if (StringUtils.isNotBlank(getCodeEquals())) {
            sqlargs.add(getCodeEquals());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.code = ?");
        }
        if (!isDisableIncluded()) {
            sqlargs.add(SystemNotifyTemplateService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(SystemNotifyTemplateService.getInstance().getFormStateField())
                    .append(" != ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT, SystemNotifyTemplateService.getInstance().getFormTable()),
                        whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL, SystemNotifyTemplateService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
