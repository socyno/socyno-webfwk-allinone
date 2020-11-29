package org.socyno.webfwk.state.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateFormService;
import org.socyno.webfwk.state.field.OptionSystemRole;
import org.socyno.webfwk.state.module.feature.SystemFeatureService;
import org.socyno.webfwk.state.module.tenant.SystemTenantBasicService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.AbstractMethodUnimplimentedException;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.*;

@Slf4j
public class PermissionService {
    
    private static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
	/**
	 * 获取用户在系统级别的角色清单
	 */
    public static Map<Long, String> getMySystemRoles() throws Exception {
        return getMyScopeRoles(AuthorityScopeType.System, null);
    }

    /**
     * 获取当前登录用户在系统级别的角色清单
     */
    public static Map<Long, OptionSystemRole> getMySystemRoleEntities() throws Exception {
        return getMyScopeRoleEntities(AuthorityScopeType.System, null);
    }
    
    /**
     * 获取用户在业务级别的角色清单
     */
    public static Map<Long, String> getMySubsystemRoles(Long subsystemId) throws Exception {
        return getMyScopeRoles(AuthorityScopeType.Subsystem, subsystemId);
    }
    
    /**
        SELECT DISTINCT
            f.feature_id
        FROM
            system_user_scope_role s,
            system_role r,
            system_role_feature f 
        WHERE
            s.user_id = ?
        AND
            r.id = s.role_id 
        AND
            r.state_form_status = 'enabled'
        AND
            r.id = f.role_id
    **/
    @Multiline
    private static final String SQL_QUERY_USER_SYSTEM_FEATURES = "X";

    /**
     * 获取当前用户在系统级别授权操作清单
     */
    public static List<String> getMyAuths() throws Exception {
        List<Long> tenantFeatures = getDao().queryAsList(Long.class, SQL_QUERY_USER_SYSTEM_FEATURES,
                new Object[] { SessionContext.getUserId() });
        if (tenantFeatures == null || tenantFeatures.size() <= 0) {
            return Collections.emptyList();
        }
        return SystemFeatureService.getTenantAuths(SessionContext.getTenant(), tenantFeatures.toArray(new Long[0]));
    }
    
    /**
     * SELECT
     *     s.scope_type,
     *     s.scope_id
     * FROM
     *     system_user_scope_role s,
     *     system_role_feature f
     * WHERE
     *     s.role_id = f.role_id
     * AND
     *     s.user_id = ?
     * AND
     *     f.feature_id in (%s)
     */
    @Multiline
    private static final String SQL_QUERY_AUTH_SCOPE_WITH_FEATURES = "X";
    
    /**
     * 查询当前用户环境下，指定授权码是通过哪些授权途径授予的。<pre>
     * 1）在给用户授权时，是通过给用户授予全局系统及某些业务系统的某些角色。
     * 2）即用户在全局系统及各业务系统中有可以被赋予不同的角色。
     * 3）用户就是通过被授予这些角色，而获取到角色中的设定的授权信息。
     * 4) 在全局系统中的授权，将被视为对租户下所有业务系统授予授权。
     * 
     * 此函数即通过已知的授权码，来查询当前用户是通过哪些授权入口获取到的该授权。
     * </pre>
     * @param authKey 授权码
     * @return
     * @throws Exception
     */
    public static Map<AuthorityScopeType, List<Long>> queryAuthScopeByKey(String authKey) throws Exception {
        if (!SessionContext.hasUserSession()) {
            return Collections.emptyMap();
        }
        List<Long> featureIds;
        if ((featureIds = SystemFeatureService.getAuthTenantFeatures(SessionContext.getTenant(), authKey)) == null
                || featureIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<Long> scopeTargetIds;
        AuthorityScopeType scopeTargetType;
        List<Map<String, Object>> scopeTargetList = getDao().queryAsList(
                String.format(SQL_QUERY_AUTH_SCOPE_WITH_FEATURES, CommonUtil.join("?", featureIds.size(), ",")),
                ArrayUtils.addAll(new Object[] { SessionContext.getUserId() }, featureIds.toArray()));
        Map<AuthorityScopeType, List<Long>> result = new HashMap<>();
        for (Map<String, Object> s : scopeTargetList) {
            if ((scopeTargetType = AuthorityScopeType.forName((String) s.get("scope_type"))) == null) {
                continue;
            }
            if ((scopeTargetIds = result.get(scopeTargetType)) == null) {
                result.put(scopeTargetType, scopeTargetIds = new ArrayList<Long>());
            }
            scopeTargetIds.add((Long) s.get("scope_id"));
        }
        return result;
    }
    
    /**
     * 查询当前用户环境下，用户在哪些业务系统中拥有指定的授权码。
     * 
     * 授权码代表着某项操作权限，那该函数查询的即为当前用户具有此操作的业务系统清单。
     * 
     * 如果当前用户为管理员，则不用进行相关的授权查询，被允许访问所有数据，此时返回 null 值。
     * 
     * 等同于 queryFormEventScopeTargetIds(AuthorityScopeType.Subsystem, authKey)
     * 
     * @param authKey
     * 
     * @return 注意： *null* - 表示有所有业务系统；*empty* - 表示在任何业务系均无此授权。
     */
    public static Long[] queryMySubsystemByAuthKey(String authKey) throws Exception {
        return queryMyScopeTargetIdsByAuthKey(AuthorityScopeType.Subsystem, authKey);
    }
    
    /**
     * 查询当前用户环境下，用户在再定授权范围的哪些授权标的中拥有指定的授权。
     * 
     * 授权码代表着某项操作权限，那该函数查询的即为当前用户具有此操作的业务系统清单。
     * 
     * 如果当前用户为管理员，则不用进行相关的授权查询，被允许访问所有数据，此时返回 null 值。
     * 
     * @param scopeType
     * 
     * @param authKey
     * 
     * @return 注意： *null* - 表示有所有业务系统；*empty* - 表示在任何业务系均无此授权。
     */
    public static Long[] queryFormEventScopeTargetIds(@NonNull AuthorityScopeType scopeType, String formName,
            String eventKey) throws Exception {
        return queryMyScopeTargetIdsByAuthKey(scopeType, AbstractStateFormService.getFormEventKey(formName, eventKey));
    }
    
    public static Long[] queryMyScopeTargetIdsByAuthKey(@NonNull AuthorityScopeType scopeType, String authKey) throws Exception {
        /* 确认用户信息是否存在，以及当前租户是否被授予该授权码 */
        if (!SessionContext.hasUserSession() || StringUtils.isBlank(authKey)
                || !SystemFeatureService.checkTenantAuth(SessionContext.getTenant(), authKey)) {
            return new Long[0];
        }
        /* 针对管理员(无论租户管理员，还是超级管理员) */
        if (SessionContext.isAdmin()) {
            return null;
        }
        Map<AuthorityScopeType, List<Long>> authScope;
        if ((authScope = queryAuthScopeByKey(authKey)) == null) {
            return new Long[0];
        }
        /* 如果在全局系统层面拥有该授权码，则意味着对租户下的所有业务系统拥有该授权 */
        if (authScope.containsKey(AuthorityScopeType.System)) {
            return null;
        }
        List<Long> scopeTargetIds;
        if ((scopeTargetIds = authScope.get(scopeType)) == null) {
            return new Long[0];
        }
        return scopeTargetIds.toArray(new Long[0]);
    }
    
    /**
        SELECT DISTINCT
            r.*
        FROM
            system_role r,
            system_user_scope_role s 
        WHERE
            s.user_id = ? 
            AND s.scope_id = 0 
            AND s.scope_type = 'System' 
            AND r.id = s.role_id
    **/
    @Multiline
    private static final String SQL_QUERY_USER_SYSTEM_ROLES = "X";

    /**
        SELECT DISTINCT
            r.*
        FROM
            system_role r,
            system_user_scope_role s 
        WHERE
            s.user_id = ? 
            AND r.id = s.role_id 
            AND s.scope_id = ?
            AND s.scope_type = 'Subsystem'
    **/
    @Multiline
    private static final String SQL_QUERY_USER_SUBSYSTEM_ROLES = "X";
    
    /**
     * 获取用户指定范围的角色清单
     */
    public static Map<Long, String> getMyScopeRoles(AuthorityScopeType scopeType, Long scopeTargetId) throws Exception {
        Map<Long, OptionSystemRole> optionSystemRoleMap = getMyScopeRoleEntities(scopeType, scopeTargetId);
        if(optionSystemRoleMap == null){
            return Collections.emptyMap();
        }
        Map<Long, String> roles = new HashMap<>();
        for (OptionSystemRole userRoleCode : optionSystemRoleMap.values()) {
            String roleCode;
            if (StringUtils.isBlank(roleCode = (String) userRoleCode.getCode())) {
                continue;
            }
            roles.put(CommonUtil.parseLong(userRoleCode.getId()), roleCode);
        }
        return roles;
    }

    /**
     * 获取用户指定范围的角色清单
     */
    public static Map<Long, OptionSystemRole> getMyScopeRoleEntities(AuthorityScopeType scopeType, Long scopeTargetId) throws Exception {
        Long userId;
        if ((userId = SessionContext.getUserId()) == null || scopeType == null) {
            return Collections.emptyMap();
        }
        List<OptionSystemRole> userRoles = null;
        if (AuthorityScopeType.System.equals(scopeType)) {
            userRoles = getDao().queryAsList(OptionSystemRole.class, SQL_QUERY_USER_SYSTEM_ROLES, new Object[] { userId });
        } else if (AuthorityScopeType.Subsystem.equals(scopeType)) {
            userRoles = getDao().queryAsList(OptionSystemRole.class,
                    String.format("%s UNION %s", SQL_QUERY_USER_SYSTEM_ROLES, SQL_QUERY_USER_SUBSYSTEM_ROLES),
                    new Object[] { userId, userId, scopeTargetId });
        }
        if (userRoles == null || userRoles.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, OptionSystemRole> roles = new HashMap<>();
        for (OptionSystemRole userRole : userRoles) {
            roles.put(CommonUtil.parseLong(userRole.getId()), userRole);
        }
        return roles;
    }
    
    /**
     *  SELECT
     *      sr.*
     *  FROM
     *      system_user_scope_role sr,
     *      system_role_feature rf
     *  WHERE
     *      sr.user_id = ?
     *  %s
     *  AND
     *      rf.role_id = sr.role_id
     *  AND
     *      rf.feature_id IN (%s)
     *  LIMIT 1
     */
    @Multiline
    private static final String SQL_CHECK_USER_SCOPE_PERMISSION = "X";
    
    /**
     *  AND (
     *          (sr.scope_type = ? AND sr.scope_id = ?)
     *      OR
     *          (sr.scope_type = 'System')
     *  )
     *  
     */
    @Multiline
    private static final String SQL_CHECK_USER_SUBSYSTEM_TMPL = "X";
    
    /**
     * 检查用户是否有接口的访问授权。
     * <pre>
     * 接口分为两类： 
     * 1，标准 HTTP 接口， 此时 authKey 即接口地址；
     * 2，通用表单的流程事件，此时 authKey 为 &lt;formName&gt;::&lt;formEvent&gt 的形式，参见 hasFormEventPermission 方法；
     * 
     * </pre>
     */
    public static boolean hasPermission(String authKey, AuthorityScopeType scopeType, Long scopeTargetId)
            throws Exception {
        /* 确认用户信息是否存在，以及授权码是否为空 */
        if (!SessionContext.hasUserSession() || StringUtils.isBlank(authKey) || scopeType == null) {
            return false;
        }
        /* 只要认证用户，即可执行 Guest 授权的操作 */
        if (AuthorityScopeType.Guest.equals(scopeType)) {
            return true;
        }
        /* 超级系统管理员，直接给与授权*/
        if (SessionContext.isAdmin() && SystemTenantBasicService.inSuperTenantContext()) {
            return true;
        }
        boolean result = false;
        if (SystemFeatureService.checkTenantAuth(SessionContext.getTenant(), authKey)) {
            /* 管理员给予所有授权 */
            if (SessionContext.isAdmin()) {
                result = true;
            } else {
                List<Long> featureIds;
                if ((featureIds = SystemFeatureService.getAuthTenantFeatures(SessionContext.getTenant(),
                        authKey)) != null && !featureIds.isEmpty()) {
                    if (AuthorityScopeType.System.equals(scopeType)) {
                        result = getDao().queryAsMap(
                                String.format(SQL_CHECK_USER_SCOPE_PERMISSION, "",
                                        CommonUtil.join("?", featureIds.size(), ",")),
                                ArrayUtils.addAll(new Object[] { SessionContext.getUserId() },
                                        featureIds.toArray())) != null;
                    } else {
                        result = getDao().queryAsMap(
                                String.format(SQL_CHECK_USER_SCOPE_PERMISSION, SQL_CHECK_USER_SUBSYSTEM_TMPL,
                                        CommonUtil.join("?", featureIds.size(), ",")),
                                ArrayUtils.addAll(
                                        new Object[] { SessionContext.getUserId(), scopeType.name(), scopeTargetId },
                                        featureIds.toArray())) != null;
                    }
                }
            }
        }
        if (ContextUtil.inDebugMode()) {
            log.info("Check permission(auth = {}) for user(admin = {}, username = {}) : result = {} ", authKey,
                    SessionContext.isAdmin(), SessionContext.getUsername(), result);
        }
        return result;
    }

    /**
     * 检查当前用户是否被授予了指定的授权码（无论是全局系统还是部分业务系统）
     * @param authKey
     * @return
     * @throws Exception
     */
    public static boolean hasAnyPermission(String authKey) throws Exception {
        return hasPermission(authKey, AuthorityScopeType.System, null);
    }
    
    /**
        SELECT DISTINCT
            s.user_id
        FROM
            system_role r,
            system_user_scope_role s 
        WHERE
            r.id = s.role_id 
            AND s.scope_id = 0 
            AND s.scope_type = 'System' 
            AND ( s.role_id = ? OR r.code = ? )
    **/
    @Multiline
    private static final String SQL_QUERY_ROLE_SYSTEM_USERS = "X";

    /**
        SELECT DISTINCT
            s.scope_id,
            s.user_id
        FROM
            system_role r,
            system_user_scope_role s 
        WHERE
             r.id = s.role_id 
            AND s.scope_type = 'Subsystem'
            AND (s.role_id = ? OR  r.code = ?)
            AND s.scope_id IN (%s)
    **/
    @Multiline
    private static final String SQL_QUERY_ROLE_SUBSYSTEM_USERS = "X";
    
    /**
     * 获取角色在指定范围的用户清单
     */
    private static Long[] getRoleScopeUserIds(Object roleIdOrCode, AuthorityScopeType scopeType, Long scopeTargetId,
            boolean includeInherited) throws Exception {
        Map<Long, Set<Long>> scopeUsers;
        if ((scopeUsers = getRoleScopeUserIds(roleIdOrCode, scopeType, new Long[] { scopeTargetId },
                includeInherited)) == null || scopeUsers.isEmpty()) {
            return null;
        }
        Set<Long> caseUsers = scopeType.checkScopeId() ? scopeUsers.get(scopeTargetId) : scopeUsers.get(0L);
        return caseUsers == null ? null : caseUsers.toArray(new Long[0]);
    }
    
    /**
     * 
     * 获取角色在指定范围的用户清单
     * @param roleIdOrCode 角色的编号或代码
     * @param scopeType 授权范围类型
     * @param scopeTargetIds 授权标的编号列表
     * @param includeInherited 是否包含继承的。从授权系统的设计上，授权当前分全局系统（System）和业务系统（Subsystem）两个
     *                           级别，当给用户在全局系统层面授予某个角色，意味着对所有业务系统都授予了该角色。这个参数的意义
     *                           就在于查询结果是否需要包含通过继承方式获得业务系统角色授权的用户。
     * @return
     * @throws Exception
     */
    private static Map<Long, Set<Long>> getRoleScopeUserIds(Object roleIdOrCode,
            AuthorityScopeType scopeType, Long[] scopeTargetIds, boolean includeInherited) throws Exception {
        if (roleIdOrCode == null || StringUtils.isBlank(roleIdOrCode.toString()) || scopeType == null) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> result = new HashMap<>();
        if (AuthorityScopeType.Subsystem.equals(scopeType)) {
            if(scopeTargetIds == null || scopeTargetIds.length <= 0) {
                return result;
            }
            Set<Long> tmpUsers;
            List<Map<String, Object>> scopeUsers = getDao().queryAsList(
                    String.format(SQL_QUERY_ROLE_SUBSYSTEM_USERS, CommonUtil.join("?", scopeTargetIds.length, ",")),
                    ArrayUtils.addAll(new Object[] { roleIdOrCode, roleIdOrCode }, (Object[]) scopeTargetIds));
            for (Map<String, Object> su : scopeUsers) {
                Long userId = (Long)su.get("user_id");
                Long scopeId = (Long)su.get("scope_id");
                if ((tmpUsers = result.get(scopeId)) == null) {
                    result.put(scopeId, tmpUsers = new HashSet<>());
                }
                tmpUsers.add(userId);
            }
        }
        List<Long> systemUsers = null;
        if (includeInherited || AuthorityScopeType.System.equals(scopeType)) {
            systemUsers = getDao()
                    .queryAsList(Long.class, SQL_QUERY_ROLE_SYSTEM_USERS, new Object[] { roleIdOrCode, roleIdOrCode });
            if (AuthorityScopeType.System.equals(scopeType)) {
                result.put(0L, new HashSet<>(systemUsers));
                return result;
            }
        }
        if (systemUsers != null && systemUsers.size() > 0 && scopeTargetIds != null) {
            for (Long scopeTargetId : scopeTargetIds) {
                if (scopeTargetId == null || result.containsKey(scopeTargetId)) {
                    continue;
                }
                result.put(scopeTargetId, new HashSet<>());
            }
            for (Set<Long> scopeUsers : result.values()) {
                scopeUsers.addAll(systemUsers);
            }
        }
        return result; 
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 包括从全局系统继承的用户
     */
    public static Long[] getRoleSubsystemUserIds(Object roleIdOrCode, Long subsystemId) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, subsystemId, true);
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 不包括从全局系统继承的用户
     */
    public static Long[] getRoleSubsystemUserIdsNoInherited(Object roleIdOrCode, Long subsystemId) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, subsystemId, false);
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 包括从全局系统继承的用户
     */
    public static Map<Long, Set<Long>> getRoleSubsystemUserIds(Object roleIdOrCode, Long[] subsystemIds) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, subsystemIds, true);
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 不包括从全局系统继承的用户
     */
    public static Map<Long, Set<Long>> getRoleSubsystemUserIdsNoInherited(Object roleIdOrCode, Long[] subsystemIds) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, subsystemIds, false);
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 包括从全局系统继承的用户
     */
    public static  Long[] getRoleSubsystemUserIds(Object roleIdOrCode, String subsystemCode) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, getDao().queryAsObject(Long.class,
                "SELECT id FROM subsystem WHERE code = ?", new Object[] { subsystemCode }), true);
    }
    
    /**
     * 获取指定业务系统中的角色用户清单, 不包括从全局系统继承的用户
     */
    public static  Long[] getRoleSubsystemUserIdsNoInherited(Object roleIdOrCode, String subsystemCode) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.Subsystem, getDao().queryAsObject(Long.class,
                "SELECT id FROM subsystem WHERE code = ?", new Object[] { subsystemCode }), false);
    }
    
    /**
     * 获取全局系统范围内指定角色的用户清单
     */
    public static  Long[] getRoleSystemUserIds(Object roleIdOrCode) throws Exception {
        return getRoleScopeUserIds(roleIdOrCode, AuthorityScopeType.System, (Long)null, true);
    }
    
    /**
     * 检查当前用户是否有指定的操作授权
     * 
     */
    public static boolean hasFormEventPermission(AuthorityScopeType scopeType, String formName, String eventKey, Long scopeTargetId) throws Exception {
        if (StringUtils.isBlank(formName) || StringUtils.isBlank(eventKey)) {
            return false;
        }
        return hasPermission(AbstractStateFormService.getFormEventKey(formName, eventKey), scopeType, scopeTargetId);
    }
    
    /**
     * 检查当前用户是否有指定的操作授权(不论全局或业务范围)
     * 
     */
    public static boolean hasFormEventAnyPermission(String formName, String eventKey) throws Exception {
        return hasFormEventPermission(AuthorityScopeType.System, formName, eventKey, null);
    }
    
    /**
     * SELECT DISTINCT
     *     s.user_id
     * FROM
     *     system_user_scope_role s,
     *     system_role_feature f
     * WHERE
     *     s.role_id = f.role_id
     * AND
     *     f.feature_id in (%s)
     * %s
     */
    @Multiline
    private static final String SQL_QUERY_SCOPE_TARGET_USERS_BY_FEATURES = "X";
    
    /**
     * 查询指定业务系统下, 拥有任一授权操作的用户清单。
     * @param subsystemId    业务系统
     * @param systemIncluded 是否包括系统全局继承而拥有这些授权的用户
     * @param authKeys       授权清单
     * @return
     * @throws Exception
     */
    public static List<Long> querySubsystemUsersByAuthKey(@NonNull Long subsystemId, boolean systemIncluded, String... authKeys)
            throws Exception {
        return querySubsystemUsersByAuthKeys(subsystemId, systemIncluded, authKeys);
    }
    
    /**
     * 查询指定业务下, 拥有任一授权的用户清单。
     * @param subsystemIds   业务系统
     * @param systemIncluded 是否包括系统全局继承而拥有这些授权的用户
     * @param authKeys       授权清单
     * @return
     * @throws Exception
     */
    private static List<Long> querySubsystemUsersByAuthKeys(long subsystemId, boolean systemIncluded, String... authKeys)
            throws Exception {
        if ((authKeys = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) authKeys)).length <= 0) {
            return Collections.emptyList();
        }
        List<Long> featureIds;
        if ((featureIds = SystemFeatureService.getAuthTenantFeatures(SessionContext.getTenant(), authKeys)) == null
                || featureIds.isEmpty()) {
            return Collections.emptyList();
        }
        String subsystemsSql = String.format("s.scope_type = 'Subsystem' AND s.scope_id = ?", subsystemId);
        return getDao().queryAsList(Long.class,
                String.format(SQL_QUERY_SCOPE_TARGET_USERS_BY_FEATURES, StringUtils.join(featureIds, ','),
                        systemIncluded
                                ? String.format(" AND ((%s) OR (s.scope_type = 'System'))", subsystemsSql)
                                : String.format(" AND %s", subsystemsSql)));
    }
    
    /**
     * 查询在给定所有(includeAll = true)或任一（includeAll = false）业务系统拥有指定操作授权的用户清单。
     * @param authKey        授权操作
     * @param systemIncluded 是否包括系统全局继承而拥有这些授权的用户
     * @param matchAll       匹配所有还是任一业务系统（System 授权视为匹配所有）
     * @param subsystemIds   业务系统
     * @return 具有授权的用户标识列表
     */
    private static List<Long> querySubsystemUsersByAuthKey(String authKey, boolean systemIncluded, boolean matchAll, long ...subsystemIds)
            throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0 || StringUtils.isBlank(authKey)) {
            return Collections.emptyList();
        }
        List<Long> featureIds;
        if ((featureIds = SystemFeatureService.getAuthTenantFeatures(SessionContext.getTenant(), authKey)) == null
                || featureIds.isEmpty()) {
            return Collections.emptyList();
        }
        String subsystemsSql = matchAll ? CommonUtil.join(
                "EXISTS(SELECT 1 FROM system_user_scope_role WHERE s.id = id AND scope_type = 'Subsystem' AND scope_id = ?)",
                subsystemIds.length, " AND "
            ) : String.format("s.scope_type = 'Subsystem' AND s.scope_id IN (%s)", CommonUtil.join("?", subsystemIds.length, ","));
        return getDao().queryAsList(Long.class,
                String.format(SQL_QUERY_SCOPE_TARGET_USERS_BY_FEATURES, StringUtils.join(featureIds, ','),
                        systemIncluded
                                ? String.format(" AND ((%s) OR (s.scope_type = 'System'))", subsystemsSql)
                                : String.format(" AND %s", subsystemsSql)), ArrayUtils.toObject(subsystemIds));
    }
    
    /**
     * 查询全局系统范围，拥有任一授权的用户清单。
     * @param authKeys       授权清单
     * @return
     * @throws Exception
     */
    private static List<Long> querySystemUsersByAuthKey(String... authKeys) throws Exception {
        List<Long> featureIds;
        if (authKeys == null || authKeys.length <= 0 || (featureIds = SystemFeatureService
                .getAuthTenantFeatures(SessionContext.getTenant(), authKeys)) == null || featureIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getDao().queryAsList(Long.class, String.format(SQL_QUERY_SCOPE_TARGET_USERS_BY_FEATURES,
                StringUtils.join(featureIds, ','), " AND s.scope_type = 'System'"));
    }
    
    /**
     * 获取授权范围内，指定表单事件的授权用户清单
     * @param scopeType
     * @param formName
     * @param eventKey
     * @param matchAllScopeIds
     * @param subsystemIds
     * @return
     * @throws Exception
     */
    public static List<Long> queryFormEventUsers(AuthorityScopeType scopeType, String formName, String eventKey,
            boolean matchAllScopeIds, long... scopeTargetIds) throws Exception {
        String authKey = AbstractStateFormService.getFormEventKey(formName, eventKey);
        if (AuthorityScopeType.System.equals(scopeType)) {
            return querySystemUsersByAuthKey(new String[] { authKey });
        }
        if (AuthorityScopeType.Subsystem.equals(scopeType)) {
            return querySubsystemUsersByAuthKey(authKey, true /* systemIncluded */, matchAllScopeIds /* matchAll */, scopeTargetIds);
        }
        throw new AbstractMethodUnimplimentedException();
    }
}
