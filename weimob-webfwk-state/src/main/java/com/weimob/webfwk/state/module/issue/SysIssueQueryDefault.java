package com.weimob.webfwk.state.module.issue;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.field.*;
import com.weimob.webfwk.state.module.issue.SysIssueFormSimple.FieldOptionsCategory;
import com.weimob.webfwk.state.module.issue.SysIssueFormSimple.FieldOptionsCloseResult;
import com.weimob.webfwk.state.module.issue.SysIssueFormSimple.FieldOptionsState;
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
@Accessors(chain=true)
@Attributes(title = "系统故障或需求申请单查询")
public class SysIssueQueryDefault extends AbstractStateFormQuery {
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "分类", type = FieldOptionsCategory.class)
    private String category;
    
    @Attributes(title = "处理人", type = FieldSystemUser.class)
    private Long assignee;
    
    @Attributes(title = "创建人", type = FieldSystemUser.class)
    private Long submitter;
    
    @Attributes(title = "处理结果", type = FieldOptionsCloseResult.class)
    private String result;
    
    @Attributes(title = "标题")
    private String title;
    
    /**
     *   SELECT i.* FROM %s i
     */
    @Multiline
    private final static String SQL_SELECT_FORM = "X";
        
    private AbstractSqlStatement buidWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        if (StringUtils.isNotBlank(getCategory())) {
            sqlArgs.add(getCategory());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.category = ?");
        }
        if (getAssignee() != null) {
            sqlArgs.add(getAssignee());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.assignee = ?");
        }
        if (StringUtils.isNotBlank(getResult())) {
            sqlArgs.add(getResult());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.result = ?");
        }
        if (StringUtils.isNotBlank(getTitle())) {
            sqlArgs.add(getTitle());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.title LIKE CONCAT('%', ?, '%')");
        }
        if (StringUtils.isNotBlank(getState())) {
            sqlArgs.add(getState());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.state_form_status = ?");
        }
        if (getSubmitter() != null) {
            sqlArgs.add(getSubmitter());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append("i.submitter = ?");
        }
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                    .setSql(StringUtils.prependIfNotEmpty(sqlWhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buidWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format(
                "SELECT COUNT(1) FROM %s i %s", SysIssueService.getInstance().getFormTable(),
                    whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buidWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format(
                SQL_SELECT_FORM.concat(" %s ORDER BY i.id DESC LIMIT %s, %s"),
                SysIssueService.getInstance().getFormTable(), whereQuery.getSql(), getOffset(), getLimit()));
    }
}