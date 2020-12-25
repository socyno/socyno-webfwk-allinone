package com.weimob.webfwk.state.module.log;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.field.*;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class SystemLogQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "操作对象")
    private String objectId;
    
    @Attributes(title = "操作类型")
    private String objectType;
    
    @Attributes(title = "操作用户", type = FieldSystemUser.class)
    private String operateUserId;
    
    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        
        if (StringUtils.isNotBlank(getObjectType())) {
            sqlArgs.add(getObjectType());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.object_type = ? ");
        }
        
        if (StringUtils.isNotBlank(getObjectId())) {
            sqlArgs.add(getObjectId());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.object_id = ? ");
        }
        
        if (StringUtils.isNotBlank(getOperateUserId())) {
            sqlArgs.add(getOperateUserId());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.operate_user_id = ? ");
        }
        
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlWhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(
                    "SELECT COUNT(1) FROM %s s %s",
                    SystemLogService.getInstance().getFormTable(),
                    query.getSql()
                ));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                    "SELECT s.* FROM %s s %s ORDER BY s.id DESC LIMIT %s, %s",
                    SystemLogService.getInstance().getFormTable(), 
                    query.getSql(), getOffset(), getLimit()
               ));
    }
}
