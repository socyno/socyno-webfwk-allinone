package com.weimob.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.state.module.user.SystemUserService;
import com.weimob.webfwk.util.state.field.FieldTableView;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.ConvertUtil;
import com.weimob.webfwk.util.tool.StringUtils;

public class FieldSystemUserAuth extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public Class<? extends FieldOptionsFilter> getDynamicFilterFormClass() {
        return FilterSystemUserAuth.class;
    }
    
    /**
        SELECT
            s.scope_type,
            '系统全局'     AS scope_type_name,
            0             AS scope_id,
            ''            AS scope_name,
            r.id          AS role_id,
            r.name        AS role_name,
            s.user_id
        FROM
            system_user_scope_role s
            INNER JOIN system_role r ON r.id = s.role_id 
        WHERE
            s.user_id IN (%s) 
            AND s.scope_type = 'System' 
            AND r.id = s.role_id
            
        UNION ALL SELECT
            s.scope_type,
            '业务系统'     AS scope_type_name,
            s.scope_id    AS scope_id,
            l.name        AS scope_name,
            r.id          AS role_id,
            r.name        AS role_name,
            s.user_id
        FROM
            system_user_scope_role s
            INNER JOIN system_role r ON r.id = s.role_id
            INNER JOIN system_business l ON s.scope_id = l.id 
        WHERE
            s.user_id IN (%s)
        AND s.scope_type = 'Business'
     */
    @Multiline
    private static final String SQL_QUERY_USER_AUTHS_BYUSERIDS = "X";
    
    /**
        SELECT DISTINCT
            'System'        AS scope_type,
            0               AS scope_id,
            '系统全局'       AS scope_type_name,
            ''              AS scope_name,
            r.id            AS role_id,
            r.name          AS role_name 
        FROM
            system_role r
        WHERE
            r.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_USER_SYSTEM_AUTHS = "X";
    
    /**
        SELECT DISTINCT
            'Business' AS scope_type,
            s.id        AS scope_id,
            '系统业务'  AS scope_type_name,
            s.name      AS scope_name,
            r.id        AS role_id,
            r.name      AS role_name 
        FROM
            system_business s
            INNER JOIN system_role r
        WHERE
            r.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_USER_BUSINESS_AUTHS = "X";
    
    /**
        AND 
            s.name LIKE CONCAT( '%', ?, '%' )
            
     */
    @Multiline
    private final static String SQL_QUERY_USER_SCOPE_KEYWORD_AUTHS = "X";
    
    /**
     * 给定输入关键字，查询可选授权清单
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemUserAuth> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        AuthorityScopeType scopeType;
        FilterSystemUserAuth keyword = (FilterSystemUserAuth)filter;
        if ((scopeType = AuthorityScopeType.forName(keyword.getScopeType())) ==  null
                    || keyword.getRoleId() == null) {
            return Collections.emptyList();
        }
        String sql = "X";
        List<Object> args = new ArrayList<>();
        if (AuthorityScopeType.System.equals(scopeType)) {
            sql = SQL_QUERY_USER_SYSTEM_AUTHS;
        } else if (AuthorityScopeType.Business.equals(scopeType)) {
            sql = SQL_QUERY_USER_BUSINESS_AUTHS;
            if (StringUtils.isNotBlank(keyword.getScopeTargetKeyword())) {
                args.add(keyword.getScopeTargetKeyword());
                sql = String.format("%s %s", SQL_QUERY_USER_BUSINESS_AUTHS, SQL_QUERY_USER_SCOPE_KEYWORD_AUTHS);
            }
        }
        args.add(0, keyword.getRoleId());
        return SystemTenantDataSource.getMain().queryAsList(OptionSystemUserAuth.class, sql, args.toArray());
    }
    
    /**
     * 给定用户的编号，查询授权清单
     * 
     * @param userIds
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemUserAuth> queryDynamicValues(Object[] userIds) throws Exception {
        Long[] idNumbers;
        if (userIds == null || userIds.length <= 0
                || (idNumbers = ConvertUtil.asNonNullUniqueLongArray(userIds)) == null || idNumbers.length <= 0) {
            return Collections.emptyList();
        }
        String placeHolder = CommonUtil.join("?", idNumbers.length, ",");
        return SystemUserService.getInstance().getFormBaseDao().queryAsList(OptionSystemUserAuth.class,
                String.format(SQL_QUERY_USER_AUTHS_BYUSERIDS, placeHolder, placeHolder),
                ArrayUtils.addAll(idNumbers, idNumbers));
    }
}
