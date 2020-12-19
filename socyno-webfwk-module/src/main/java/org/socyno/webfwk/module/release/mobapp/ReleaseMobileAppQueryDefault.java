package org.socyno.webfwk.module.release.mobapp;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
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
@Attributes(title = "移动端应用配置查询")
public class ReleaseMobileAppQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "系统类型", position = 1001, type = ReleaseMobileAppFormDetail.FieldOptionsAppStoreType.class)
    private String storeType;
    
    @Attributes(title = "我添加的", position = 1002)
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
        
        if (StringUtils.isNotBlank(getStoreType())) {
            sqlargs.add(getStoreType());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.store_type = ? ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT.concat(" %s"),
                ReleaseMobileAppService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        ReleaseMobileAppService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
