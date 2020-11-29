package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import lombok.*;
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
@Accessors(chain = true)
@Attributes(title = "上架应用商店列表查询")
public class ReleaseMobileOnlineDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", type = ReleaseMobileOnlineDetailFrom.FieldOptionsState.class, position = 1001)
    private String state;
    
    @Attributes(title = "应用", type = FieldReleaseMobileOnlineApplication.class, position = 1002)
    private String applicationName;
    
    @Attributes(title = "我申请的", position = 1003)
    private boolean createdByMe;
    
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
        
        if (StringUtils.isNotBlank(getState())) {
            sqlargs.add(getState());
            sqlstmt.append(String.format(" and a.%s = ?", ReleaseMobileOnlineService.DEFAULT.getFormStateField()));
        }
        
        if (StringUtils.isNotBlank(getApplicationName())) {
            sqlargs.add(getApplicationName());
            sqlstmt.append(" and a.application_name = ?");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt.toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_COUNT + " %s", ReleaseMobileOnlineService.DEFAULT.getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM + " %s ORDER BY a.id DESC LIMIT %s, %s",
                        ReleaseMobileOnlineService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
