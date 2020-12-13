package org.socyno.webfwk.state.module.todo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;
import org.socyno.webfwk.util.state.field.*;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "待办实现查询")
public class SystemTodoQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", position = 10, type = SystemTodoFormSimple.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "起始创建日期", position = 20, type = FieldDateOnly.class)
    private Date createdAtBegin;
    
    @Attributes(title = "结束创建日期", position = 30, type = FieldDateOnly.class)
    private Date createdAtEnd;
    
    @Attributes(title = "类型", position = 40)
    private String category;
    
    @Attributes(title = "审批人包含", position = 50, type = FieldSystemUser.class)
    private Long assignee;
    
    @Attributes(title = "流程单编号")
    private String targetId;
    
    @Attributes(title = "最终审批人", position = 60, type = FieldSystemUser.class)
    private Long closedUserId;
    
    @Attributes(title = "待办项标识")
    private String targetKey;

    @Attributes(title = "待办流程发起人", position = 70, type = FieldSystemUser.class)
    private Long applyUserId ;

    @Attributes(title = "待办事项创建人", position = 80, type = FieldSystemUser.class)
    private Long createdUserId ;
    
    public SystemTodoQueryDefault() {
        super();
    }
    
    public SystemTodoQueryDefault(long page, int limit) {
        super();
        setPage(page);
        setLimit(limit);
    }
    
    /**
     SELECT
        f.*
     FROM
        %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_TODOS = "X";
    
    /**
     SELECT
        COUNT(1)
     FROM
        %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_TODOS = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(category)) {
            sqlargs.add(category);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.category = ?");
        }
        if (StringUtils.isNotBlank(targetId)) {
            sqlargs.add(targetId);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.target_id = ?");
        }
        if (StringUtils.isNotBlank(targetKey)) {
            sqlargs.add(targetKey);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.target_key = ?");
        }
        if (StringUtils.isNotBlank(state)) {
            sqlargs.add(state);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.state_form_status = ?");
        }
        if (closedUserId != null) {
            sqlargs.add(closedUserId);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.closed_user_id = ?");
        }
        if (createdAtEnd != null) {
            sqlargs.add(DateFormatUtils.format(createdAtEnd, "yyyy-MM-dd 24:00:00"));
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_at < ?");
        }
        if (createdAtBegin != null) {
            sqlargs.add(DateFormatUtils.format(createdAtBegin, "yyyy-MM-dd 00:00:00"));
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_at >= ?");
        }
        if(createdUserId!=null){
            sqlargs.add(createdUserId);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_user_id = ?");
        }
        if (assignee != null) {
            sqlargs.add(assignee);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(
                    "EXISTS (SELECT a.todo_id FROM system_common_todo_assignee a WHERE a.todo_id = f.id AND a.todo_user = ?)");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT_TODOS, SystemTodoService.getInstance().getFormTable()),
                        whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL_TODOS, SystemTodoService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
