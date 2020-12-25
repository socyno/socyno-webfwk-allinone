package com.weimob.webfwk.state.module.access;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.context.SessionContext;
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
@Attributes(title = "权限申请列表查询")
public class SystemAccessApplyQueryDefault extends AbstractStateFormQuery {

    @Attributes(title = "我发起的申请")
    private boolean createdByMe;

    @Attributes(title = "状态", type = SystemAccessApplyFormDetail.FieldOptionsState.class)
    private String state;

    /**
     * SELECT a.* FROM %s a
     **/
    @Multiline
    public final static String SQL_SELECT_FORM = "X";

    /**
     * SELECT COUNT(1) FROM %s a
     **/
    @Multiline
    public final static String SQL_SELECT_COUNT = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (createdByMe == true) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.created_code_by = ? ");
            sqlargs.add(SessionContext.getUsername());
        }
        
        if (StringUtils.isNotBlank(getState())) {
            sqlargs.add(getState());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("a.%s = ?", SystemAccessApplyService.getInstance().getFormStateField()));
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_COUNT.concat(" %s"), SystemAccessApplyService.getInstance().getFormTable(),
                        query.getSql()));
    }

    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        SystemAccessApplyService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
