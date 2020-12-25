package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.module.role.SystemRoleService;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

public class FieldSystemBusinessOwned extends FieldSystemBusinessAll {
    
    /**
        SELECT DISTINCT
            b.*
        FROM
            system_business b,
            system_user_scope_role s,
            system_role r
        WHERE
            s.user_id = ?
        AND
            s.scope_type = 'Business'
        AND
            s.role_id = r.id
        AND
            s.scope_id = b.id
        AND
            r.code IN (?, ?)
     */
    @Multiline
    private final static String SQL_QUERY_MANAGEMENT_BUSINESS = "X";
    
    /**
        (
                b.code LIKE CONCAT('%', ?, '%')
            OR
                b.name LIKE CONCAT('%', ?, '%')
            OR
                b.description LIKE CONCAT('%', ?, '%')
        )
     */
    @Multiline
    private final static String SQL_WHERE_BUSINESS_KEYWORD = "X";
    
    @Override
    public List<OptionSystemBusiness> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        if (SessionContext.isAdmin()) {
            return super.queryDynamicOptions(filter);
        }
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (keyword != null && StringUtils.isNotBlank(keyword.getKeyword())) {
            sqlargs.add(keyword.getKeyword());
            sqlargs.add(keyword.getKeyword());
            sqlargs.add(keyword.getKeyword());
            sqlwhere.append(" AND ").append(SQL_WHERE_BUSINESS_KEYWORD);
        }
        sqlargs.add(0, SystemRoleService.InternalRoles.Owner.getCode());
        sqlargs.add(0, SystemRoleService.InternalRoles.Admin.getCode());
        sqlargs.add(0, SessionContext.getUserId());
        return SystemTenantDataSource.getMain().queryAsList(OptionSystemBusiness.class,
                String.format(SQL_QUERY_MANAGEMENT_BUSINESS, sqlwhere.toString()),
                sqlargs.toArray());
    }
}
