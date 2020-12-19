package org.socyno.webfwk.module.syslock;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统分布式锁列表查询")
public class SystemLockQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "只显已锁定的", position = 1001)
    private boolean locking;
    
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
        StringBuilder sqlwhere = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (locking == true) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.locked is not null ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT.concat(" %s"),
                SystemLockService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        SystemLockService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
