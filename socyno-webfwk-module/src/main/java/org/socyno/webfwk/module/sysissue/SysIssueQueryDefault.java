package org.socyno.webfwk.module.sysissue;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.sysissue.SysIssueFormSimple.FieldOptionsCategory;
import org.socyno.webfwk.module.sysissue.SysIssueFormSimple.FieldOptionsCloseResult;
import org.socyno.webfwk.module.sysissue.SysIssueFormSimple.FieldOptionsState;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

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
    @Attributes(title = "状态", position = 1010, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "分类", position = 1011, type = FieldOptionsCategory.class)
    private String category;
    
    @Attributes(title = "处理人", position = 1020, type = FieldSystemUser.class)
    private Long assignee;
    
    @Attributes(title = "创建人", position = 1021, type = FieldSystemUser.class)
    private Long submitter;
    
    @Attributes(title = "处理结果", position = 1030, type = FieldOptionsCloseResult.class)
    private String result;
    
    @Attributes(title = "标题", position = 1031)
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