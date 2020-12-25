package com.weimob.webfwk.module.release.build;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.module.user.SystemUserService;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "构建服务查询")
public class SystemBuildQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "类型", position = 1010, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "关键字", position = 1010)
    private String keyword;
    
    @Attributes(title = "是否包括已禁用", position = 1020)
    private boolean disableIncluded = false;
    
    public SystemBuildQueryDefault() {
        super();
    }
    
    public SystemBuildQueryDefault(long page, int limit) {
        super(limit, page);
    }
    
    /**
     * SELECT COUNT(1) FROM %s b
     */
    @Multiline
    public final static String SQL_QUERY_COUNT_BUILDS = "X";
    
    /**
     * SELECT b.* FROM %s b
     */
    @Multiline
    public final static String SQL_QUERY_ALL_BUILDS = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getType())) {
            sqlargs.add(getType());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("b.type = ? ");
        }
        
        if (StringUtils.isNotBlank(getKeyword())) {
            sqlargs.add(getKeyword());
            sqlargs.add(getKeyword());
            sqlargs.add(getKeyword());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(
                    "( b.code LIKE CONCAT('%', ?, '%')  OR b.title LIKE CONCAT('%', ?, '%') OR b.description LIKE CONCAT('%', ?, '%') )");
        }
        
        if (!disableIncluded) {
            sqlargs.add(SystemUserService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("b.%s != ?", SystemBuildService.getInstance().getFormStateField()));
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY b.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL_BUILDS, SystemBuildService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT_BUILDS, SystemBuildService.getInstance().getFormTable()),
                        whereQuery.getSql()));
    }
    
}
