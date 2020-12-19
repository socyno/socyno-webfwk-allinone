package org.socyno.webfwk.state.module.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统用户查询")
public class SystemUserQueryDefault extends AbstractStateFormQuery {
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_USERS = "X";
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_USERS = "X";
    
    /**
     (
         f.username LIKE CONCAT('%', ?, '%')
       OR
         f.display LIKE CONCAT('%', ?, '%')
       OR
         f.mail_address LIKE CONCAT('%', ?, '%')
     )
    */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_USERS_TMPL = "X";
    
    /**
        EXISTS (
            SELECT
                x.user_id
            FROM
                system_user_scope_role x
            WHERE
                f.id = x.user_id
            AND
                x.role_id = ?
            %s
        )
    */
    @Multiline
    private static final String SQL_QUERY_ROLE_USERS_TMPL = "X";
    
    /**
        EXISTS (
           SELECT
               x.user_id
           FROM
               system_user_scope_role x
           WHERE
               f.id = x.user_id
           AND
               x.scope_type = ?
        )
    */
    @Multiline
    private static final String SQL_QUERY_SCOPE_USERS_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "授权范围", position = 1030, type = FilterSystemUserAuth.FieldOptionsScopeType.class)
    private String permScope;
    
    @Attributes(title = "授权角色", position = 1040, type = FieldSystemRole.class)
    private Long permRoleId;
    
    @Attributes(title = "是否包括已禁用", position = 1060)
    private boolean disableIncluded = false;
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private Long manager;
    
    @Attributes(title = "编号清单", description = "以提供的用户编号进行查询，多个可使用逗号、空格或分号进行分割")
    private String idsIn;
    
    @Attributes(title = "帐户清单", description = "以提供的用户帐户进行查询，多个可使用逗号、空格或分号进行分割")
    private String namesIn;
    
    public SystemUserQueryDefault(long page, int limit) {
        this(null, page, limit);
    }
    
    public SystemUserQueryDefault(String nameLike, long page, int limit) {
        this.nameLike = nameLike;
        this.setPage(page);
        this.setLimit(limit);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlstmt = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(nameLike)) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_NAMELIKE_USERS_TMPL);
        }
        
        if (!isDisableIncluded()) {
            sqlargs.add(SystemUserService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SystemUserService.getInstance().getFormStateField())
                    .append(" != ?");
        }
        
        if (getPermRoleId() != null) {
            String scopePlaced = "";
            sqlargs.add(getPermRoleId());
            if (StringUtils.isNotBlank(getPermScope())) {
                sqlargs.add(getPermScope());
                scopePlaced = " AND x.scope_type = ?";
            }
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ")
                    .append(String.format(SQL_QUERY_ROLE_USERS_TMPL, scopePlaced));
        } else if (StringUtils.isNotBlank(getPermScope())) {
            sqlargs.add(getPermScope());
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_SCOPE_USERS_TMPL);
        }
        
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] ids;
            if ((ids = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && ids.length > 0) {
                sqlargs.addAll(Arrays.asList(ids));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.")
                        .append(SystemUserService.getInstance().getFormIdField())
                        .append(CommonUtil.join("?", ids.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        
        if (StringUtils.isNotBlank(getNamesIn())) {
            String[] names;
            if ((names = CommonUtil.split(getNamesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED)) != null
                    && names.length > 0) {
                sqlargs.addAll(Arrays.asList(names));
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.username")
                        .append(CommonUtil.join("?", names.length, ",", " IN (", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("0 = 1");
            }
        }
        
        if (manager != null) {
            sqlargs.add(manager);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("f.manager = ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                String.format(SQL_QUERY_COUNT_USERS, SystemUserService.getInstance().getFormTable()), whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL_USERS, SystemUserService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
