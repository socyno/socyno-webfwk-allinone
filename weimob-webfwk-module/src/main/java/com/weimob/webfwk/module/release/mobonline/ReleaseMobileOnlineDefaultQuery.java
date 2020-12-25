package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.*;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(chain = true)
@Attributes(title = "上架应用商店列表查询")
public class ReleaseMobileOnlineDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", type = ReleaseMobileOnlineFormDetail.FieldOptionsState.class, position = 1001)
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
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (createdByMe) {
            sqlargs.add(SessionContext.getUsername());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.created_code_by = ? ");
        }
        
        if (StringUtils.isNotBlank(getState())) {
            sqlargs.add(getState());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("a.%s = ?", ReleaseMobileOnlineService.getInstance().getFormStateField()));
        }
        
        if (StringUtils.isNotBlank(getApplicationName())) {
            sqlargs.add(getApplicationName());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.application_name = ?");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT.concat(" %s"),
                ReleaseMobileOnlineService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        ReleaseMobileOnlineService.getInstance().getFormTable(), query.getSql(), getOffset(),
                        getLimit()));
    }
}
