package com.weimob.webfwk.state.module.lock;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统分布式锁查询")
public class SystemLockQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "仅显锁定中的")
    private boolean locking = false;
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlwhere = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (locking) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.locked is not null ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT COUNT(1) FROM %s a %s",
                SystemLockService.getInstance().getFormTable(),
                query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                    "SELECT a.* FROM %s a %s ORDER BY a.id DESC LIMIT %s, %s",
                    SystemLockService.getInstance().getFormTable(), 
                    query.getSql(), 
                    getOffset(), 
                    getLimit()
                ));
    }
}
