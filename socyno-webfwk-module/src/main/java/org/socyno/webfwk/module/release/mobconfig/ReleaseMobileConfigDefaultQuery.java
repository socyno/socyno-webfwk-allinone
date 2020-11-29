package org.socyno.webfwk.module.release.mobconfig;

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
@Attributes(title = "移动端应用配置查询")
public class ReleaseMobileConfigDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "系统类型", position = 1001, type = ReleaseMobileConfigDetail.FieldOptionsAppStoreType.class)
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
        StringBuffer sqlstmt = new StringBuffer("where 1 = 1");
        List<Object> sqlargs = new ArrayList<>();
        if (createdByMe == true) {
            sqlstmt.append(" and a.created_code_by = ? ");
            sqlargs.add(SessionContext.getUsername());
        }
        
        if (StringUtils.isNotBlank(getStoreType())) {
            sqlargs.add(getStoreType());
            sqlstmt.append(" and a.store_type = ? ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(sqlstmt.toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT + " %s",
                ReleaseMobileConfigService.DEFAULT.getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM + " %s ORDER BY a.id DESC LIMIT %s, %s",
                        ReleaseMobileConfigService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
