package org.socyno.webfwk.module.sysaccess;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "权限申请列表查询")
public class SystemAccessApplyListDefaultQuery extends AbstractStateFormQuery {

    @Attributes(title = "只显示我的任务", position = 1001)
    private boolean createdByMe;

    @Attributes(title = "状态", type = SystemAccessApplyDetail.FieldOptionsState.class , position = 1002)
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
        StringBuffer sqlstmt = new StringBuffer("where 1 = 1");
        List<Object> sqlargs = new ArrayList<>();
        if (createdByMe == true) {
            sqlstmt.append(" and a.created_code_by = ? ");
            sqlargs.add(SessionContext.getUsername());
        }

        if(StringUtils.isNotBlank(getState())){
            sqlargs.add(getState());
            sqlstmt.append(String.format(" and a.%s = ?", SystemAccessApplyService.DEFAULT.getFormStateField()));
        }

        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt.toString());
    }


    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_COUNT + " %s", SystemAccessApplyService.DEFAULT.getFormTable(),
                        query.getSql()));
    }

    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_FORM + " %s ORDER BY a.id DESC LIMIT %s, %s",
                        SystemAccessApplyService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
