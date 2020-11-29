package org.socyno.webfwk.state.module.feature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.FieldSystemRole;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.Getter;
import lombok.NonNull;
public class SystemFeatureService extends AbstractStateFormServiceWithBaseDao<SystemFeatureDetail> {
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "有效")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public static String[] stringify(STATES... states) {
            if (states == null || states.length <= 0) {
                return new String[0];
            }
            String[] result = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                result[i] = states[i].getCode();
            }
            return result;
        }
        
        public static String[] stringifyEx(STATES... states) {
            if (states == null) {
                states = new STATES[0];
            }
            List<String> result = new ArrayList<>(states.length);
            for (STATES s : STATES.values()) {
                if (!ArrayUtils.contains(states, s)) {
                    result.add(s.getCode());
                }
            }
            return result.toArray(new String[0]);
        }

        public static List<? extends FieldOption> getStatesAsOption() {
            List<FieldOption> options = new ArrayList<>();
            for (STATES s : STATES.values()) {
                options.add(new FieldSimpleOption(s.getCode(), s.getName()));
            }
            return options;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemFeatureListDefaultForm>("default", 
                SystemFeatureListDefaultForm.class, SystemFeatureListDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Submit(new AbstractStateSubmitAction<SystemFeatureDetail, SystemFeatureForCreation>("创建", STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemFeatureDetail form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, SystemFeatureDetail originForm, SystemFeatureForCreation form, String message)
                    throws Exception {
                final AtomicLong id = new AtomicLong(-1);
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareInsertQuery(DEFAULT.getFormTable(),
                                        new ObjectMap()
                                                .put("code", form.getCode())
                                                .put("name", form.getName())
                                                .put("description", CommonUtil.ifNull(form.getDescription(), ""))
                                                .put("created_by", SessionContext.getDisplay())
                            ), new ResultSetProcessor() {
                                @Override
                                public void process(ResultSet r, Connection c) throws Exception {
                                    r.next();
                                    id.set(r.getLong(1));
                                    List<OptionSystemAuth> auths;
                                    if ((auths = form.getAuths()) != null) {
                                        setFeatureAuths(id.get(), auths);
                                    }
                                }
                            });
                    }
                });
                return id.get();
            }
        }),
        Update(new AbstractStateAction<SystemFeatureDetail, SystemFeatureForEdition, Void>("编辑", STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemFeatureDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemFeatureDetail originForm, final SystemFeatureForEdition form,
                    final String message) throws Exception {
                getDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet result, Connection conn) throws Exception {
                        List<OptionSystemAuth> auths;
                        DEFAULT.getFormBaseDao()
                                .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                        new ObjectMap()
                                                .put("=id", form.getId())
                                                .put("name", form.getName())
                                                .put("description", CommonUtil.ifNull(form.getDescription(), ""))));
                        
                        if ((auths = form.getAuths()) != null) {
                            setFeatureAuths(form.getId(), auths);
                        }
                    }
                });
                return null;
            }
        }),
        Delete(new AbstractStateDeleteAction<SystemFeatureDetail>("删除", STATES.stringifyEx()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemFeatureDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemFeatureDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(DEFAULT.getFormTable(),
                        new ObjectMap().put("=id", originForm.getId())));
                setFeatureAuths(originForm.getId(), Collections.emptyList());
                return null;
            }
        });
        
        private final AbstractStateAction<SystemFeatureDetail, ?, ?> action;
        
        EVENTS(AbstractStateAction<SystemFeatureDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SystemFeatureDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    public static final SystemFeatureService DEFAULT = new SystemFeatureService();
    
    @Override
    public String getFormName() {
        return getName();
    }
    
    @Override
    protected String getFormTable() {
        return getTable();
    }
    
    protected static String getTable() {
        return "system_feature";
    }
    
    protected static String getName() {
        return "system_feature";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemFeatureDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemFeatureDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    private static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
     * 检索功能清单（支持模糊检索, 不包括关联授权数据）。
     * 
     * @param nameLike 检索的关键字
     */
    public static PagedList<SystemFeatureSimple> query(String nameLike, long page, int limit) throws Exception {
        SystemFeatureListDefaultQuery query = new SystemFeatureListDefaultQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setNameLike(nameLike);
        return DEFAULT.listFormX(SystemFeatureSimple.class, query);
    }
    
    /**
     * 检索功能清单（支持模糊检索, 不包括关联授权数据）。
     * 
     * @param nameLike 检索的关键字
     */
    public static PagedList<SystemFeatureSimple> queryWithTenant(String tenant, String nameLike, long page, int limit) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return null;
        }
        SystemFeatureListTenantQuery query = new SystemFeatureListTenantQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setNameLike(nameLike);
        query.setTenantCode(tenant);
        return DEFAULT.listFormX(SystemFeatureSimple.class, query);
    }
    
    /**
     * 获取全部的功能清单。
     */
    public static PagedList<SystemFeatureSimple> query(int page, int limit) throws Exception {
        return query(null, page, limit);
    }
    
    /**
     * 获取功能详情数据，包括授权信息。
     */
    public static SystemFeatureDetail get(Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<SystemFeatureDetail> result;
        if (idOrCode.toString().matches("^\\d+$")) {
            result = queryByIds(SystemFeatureDetail.class, CommonUtil.parseLong(idOrCode));
        } else {
            result = queryByCodes(SystemFeatureDetail.class, idOrCode.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return withAuths(result.get(0));
    }
    
    /**
     * 重写获取表单详情的方法, 载如关联的授权清单
     * 
     */
    @Override
    public SystemFeatureDetail getForm(long formId) throws Exception {
        return get(formId);
    }
    
    /**
     * SELECT DISTINCT
     *     a.feature_id 
     * FROM
     *     system_feature_auth a,
     *     system_tenant_feature f,
     *     system_tenant t
     * WHERE
     *     f.feature_id = a.feature_id
     * AND
     *     t.id = f.tenant_id
     * AND
     *     t.code = ?
     * AND
     *     a.auth_key IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_AUTH_FEATURES = "X";
    
    /**
     * 包含指定接口或操作的功能列表
     */
    public static List<Long> getAuthTenantFeatures(String tenant, String ...authKeys) throws Exception {
        if (authKeys == null || authKeys.length <= 0 || StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return DEFAULT.getFormBaseDao().queryAsList(Long.class,
                String.format(SQL_QUERY_AUTH_FEATURES, CommonUtil.join("?", authKeys.length, ",")),
                ArrayUtils.addAll(new Object[] { tenant }, (Object[])authKeys));
    }
    
    /**
     * SELECT DISTINCT
     *     f.feature_id 
     * FROM
     *     system_feature f,
     *     system_tenant  t,
     *     system_tenant_feature tf
     * WHERE
     *     t.id = tf.tenant_id
     * AND
     *     f.id = tf.feature_id
     * AND
     *     t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES = "X";
    
    /**
     * 获取租户的功能清单
     */
    public static List<Long> getTenantFeatures(String tenant) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return DEFAULT.getFormBaseDao().queryAsList(Long.class, SQL_QUERY_TENANT_FEATURES, new Object[] { tenant });
    }
    
    /**
     * SELECT DISTINCT
     *     a.auth_key 
     * FROM
     *     system_feature f,
     *     system_tenant  t,
     *     system_tenant_feature tf,
     *     system_feature_auth a
     * WHERE
     *     t.id = tf.tenant_id
     * AND
     *     f.id = tf.feature_id
     * AND
     *     a.feature_id = f.id
     * AND
     *     t.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_AUTHS = "X";
    
    /**
     * 获取租户的在给定功能中的所有接口清单 
     */
    public static List<String> getTenantAuths(String tenant, Long... features) throws Exception {
        if (StringUtils.isBlank(tenant) || features == null || features.length <= 0) {
            return Collections.emptyList();
        }
        return DEFAULT.getFormBaseDao().queryAsList(String.class,
                String.format("%s AND tf.feature_id IN (%s)", SQL_QUERY_TENANT_AUTHS,
                        CommonUtil.join("?", features.length, ",")),
                ArrayUtils.addAll(new Object[] { tenant }, (Object[])features));
    }

    /**
     * 获取租户的所有授权操作清单 
     */
    public static List<String> getTenantAllAuths(String tenant) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return DEFAULT.getFormBaseDao().queryAsList(String.class, SQL_QUERY_TENANT_AUTHS, new Object[] { tenant });
    }
    
    /**
     * 获取租户下是否拥有指定的授权操作
     */
    public static boolean checkTenantAuth(String tenant, String authKey) throws Exception {
        if (StringUtils.isBlank(tenant) || StringUtils.isBlank(authKey)) {
            return false;
        }
        return DEFAULT.getFormBaseDao().queryAsList(String.class,
                String.format("%s AND a.auth_key = ?", SQL_QUERY_TENANT_AUTHS),
                new Object[] { tenant, authKey }).size() > 0;
    }
    
    /**
     * SELECT f.* FROM %s f WHERE f.id IN (%s) ORDER BY f.id DESC
     */
    @Multiline
    private static final String SQL_QUERY_ID_FEATURES = "X";
    
    /**
     * 通过给定的编号列表，检索系统功能清单（不包括关联授权信息）。
     */
    public static <T extends SystemFeatureSimple> List<T> queryByIds(@NonNull Class<T> clazz, final Long... ids) throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return DEFAULT.queryFormWithStateRevision(clazz, String.format(SQL_QUERY_ID_FEATURES, 
                         DEFAULT.getFormTable(), CommonUtil.join("?", ids.length, ",")), ids);
    }
    
    /**
     * SELECT DISTINCT
     *     f.*
     * FROM
     *     %s f,
     *     system_tenant_feature tf,
     *     system_tenant t
     * WHERE
     *     f.id = tf.feature_id
     * AND
     *     t.id = tf.tenant_id
     * AND
     *     t.code = ?
     * AND 
     *     f.id IN (%s)
     * ORDER BY
     *     f.id DESC
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES_BYID = "X";
    
    /**
     * 通过给定的编号列表，检索租户功能清单（不包括关联授权信息）。
     */
    public static <T extends SystemFeatureSimple> List<T> queryByIdsWithTenant(@NonNull Class<T> clazz, String tenant,
            final Long... ids) throws Exception {
        if (StringUtils.isBlank(tenant) || ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return DEFAULT
                .queryFormWithStateRevision(clazz,
                        String.format(SQL_QUERY_TENANT_FEATURES_BYID, DEFAULT.getFormTable(),
                                CommonUtil.join("?", ids.length, ",")),
                        ArrayUtils.addAll(new Object[] { tenant }, (Object[])ids));
    }
    
    /**
     * SELECT f.* FROM %s f WHERE f.code IN (%s) ORDER BY f.code DESC
     */
    @Multiline
    private static final String SQL_QUERY_CODE_FEATURES = "X";

    /**
     * 通过给定的编号列表，检索系统功能清单（不包括关联授权信息）。
     */
    public static <T extends SystemFeatureSimple> List<T> queryByCodes(@NonNull Class<T> clazz, final String... roleCodes) throws Exception {
        if (roleCodes == null || roleCodes.length <= 0) {
            return Collections.emptyList();
        }
        return DEFAULT.queryFormWithStateRevision(clazz, String.format(SQL_QUERY_CODE_FEATURES, 
                DEFAULT.getFormTable(), CommonUtil.join("?", roleCodes.length, ",")), roleCodes);
    }
    
    /**
     * SELECT DISTINCT
     *     a.auth_key
     * FROM
     *     system_feature f,
     *     system_feature_auth a
     * WHERE
     *     f.id = a.feature_id
     * AND
     *     f.id = ?
     */
    @Multiline
    private static final String SQL_QUERY_FEATURE_AUTHS = "X";
    
    /**
     * SELECT DISTINCT
     *     a.role_id
     * FROM
     *     system_feature f,
     *     system_role_feature a
     * WHERE
     *     f.id = a.feature_id
     * AND
     *     f.id = ?
     */
    @Multiline
    private static final String SQL_QUERY_FEATURE_ROLES = "X";
    
    /**
     * 填充功能详情的授权数据
     * @param formId
     * @param auths
     * @throws Exception
     */
    private static SystemFeatureDetail withAuths(SystemFeatureDetail form) throws Exception {
        if (form == null) {
            return null;
        }
        if (form.getId() != null) {
            form.setAuths(Collections.emptyList());
            List<String> auths = getDao().queryAsList(String.class, SQL_QUERY_FEATURE_AUTHS,
                    new Object[] { form.getId() });
            if (auths != null && !auths.isEmpty()) {
                form.setAuths(ClassUtil.getSingltonInstance(FieldSystemAuths.class)
                        .queryDynamicValues(auths.toArray(new String[0])));
            }
            List<Long> roles = getDao().queryAsList(Long.class, SQL_QUERY_FEATURE_ROLES, new Object[] { form.getId() });
            if (roles != null && !roles.isEmpty()) {
                form.setRoles(ClassUtil.getSingltonInstance(FieldSystemRole.class).queryDynamicValues(roles.toArray()));
            }
        }
        return form;
    }
    
    /**
     * 存储功能的授权数据
     * @param formId
     * @param auths
     * @throws Exception
     */
    private static void setFeatureAuths(long formId, List<OptionSystemAuth> auths) throws Exception {
        if (auths == null) {
            return;
        }
        DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_feature_auth",
                        new ObjectMap().put("=feature_id", formId)));
                for (OptionSystemAuth auth : auths) {
                    if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                        continue;
                    }
                    DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            "system_feature_auth",
                            new ObjectMap().put("feature_id", formId).put("=auth_key", auth.getOptionValue())));
                }
            }
        });
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }

    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
}
