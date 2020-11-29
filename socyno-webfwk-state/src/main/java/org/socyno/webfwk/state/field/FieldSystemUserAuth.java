package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;

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
            INNER JOIN subsystem l ON s.scope_id = l.id 
        WHERE
            s.user_id IN (%s)
        AND s.scope_type = 'Subsystem'
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
            'Subsystem' AS scope_type,
            s.id        AS scope_id,
            '业务系统'  AS scope_type_name,
            s.name      AS scope_name,
            r.id        AS role_id,
            r.name      AS role_name 
        FROM
            subsystem s
            INNER JOIN system_role r
        WHERE
            r.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_USER_SUBSYSTEM_AUTHS = "X";
    
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
    public List<OptionSystemUserAuth> queryDynamicOptions(FilterSystemUserAuth filter) throws Exception {
        AuthorityScopeType scopeType;
        if ((scopeType = AuthorityScopeType.forName(filter.getScopeType())) ==  null
                    || filter.getRoleId() == null) {
            return Collections.emptyList();
        }
        String sql = "X";
        List<Object> args = new ArrayList<>();
        if (AuthorityScopeType.System.equals(scopeType)) {
            sql = SQL_QUERY_USER_SYSTEM_AUTHS;
        } else if (AuthorityScopeType.Subsystem.equals(scopeType)) {
            sql = SQL_QUERY_USER_SUBSYSTEM_AUTHS;
            if (StringUtils.isNotBlank(filter.getScopeTargetKeyword())) {
                args.add(filter.getScopeTargetKeyword());
                sql = String.format("%s %s", SQL_QUERY_USER_SUBSYSTEM_AUTHS, SQL_QUERY_USER_SCOPE_KEYWORD_AUTHS);
            }
        }
        args.add(0, filter.getRoleId());
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
        return SystemTenantDataSource.getMain().queryAsList(OptionSystemUserAuth.class,
                String.format(SQL_QUERY_USER_AUTHS_BYUSERIDS, placeHolder, placeHolder),
                ArrayUtils.addAll(idNumbers, idNumbers));
    }
}
