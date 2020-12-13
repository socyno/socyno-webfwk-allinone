package org.socyno.webfwk.state.module.feature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
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

import lombok.Getter;
import lombok.NonNull;

public class SystemFeatureService extends
        AbstractStateFormServiceWithBaseDao<SystemFeatureFormDetail, SystemFeatureFormDefault, SystemFeatureFormSimple> {
    
    @Getter
    private static final SystemFeatureService Instance = new SystemFeatureService();
    
    private SystemFeatureService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "有效")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemFeatureFormDefault>("默认查询", 
                SystemFeatureFormDefault.class, SystemFeatureQueryDefault.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateSubmitAction<SystemFeatureFormDetail, SystemFeatureFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Long handle(String event, SystemFeatureFormDetail originForm, SystemFeatureFormCreation form, String message)
                throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareInsertQuery(getFormTable(),
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
    }
    
    public class EventUpdate extends AbstractStateAction<SystemFeatureFormDetail, SystemFeatureFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemFeatureFormDetail originForm, final SystemFeatureFormEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    List<OptionSystemAuth> auths;
                    getFormBaseDao()
                            .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
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
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemFeatureFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemFeatureFormDetail originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())));
            setFeatureAuths(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Update(EventUpdate.class),
        Delete(EventDelete.class);
        
        private final Class<? extends AbstractStateAction<SystemFeatureFormDetail, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemFeatureFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
     
    
    @Override
    public String getFormName() {
        return "system_feature";
    }
    
    @Override
    protected String getFormTable() {
        return "system_feature";
    }
    
    @Override
    public String getFormDisplay() {
        return "业务系统";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
     * 检索功能清单（支持模糊检索, 不包括关联授权数据）。
     * 
     * @param nameLike 检索的关键字
     */
    public PagedList<SystemFeatureFormSimple> query(String nameLike, long page, int limit) throws Exception {
        SystemFeatureQueryDefault query = new SystemFeatureQueryDefault();
        query.setPage(page);
        query.setLimit(limit);
        query.setNameLike(nameLike);
        return listForm(SystemFeatureFormSimple.class, query);
    }
    
    /**
     * 检索功能清单（支持模糊检索, 不包括关联授权数据）。
     * 
     * @param nameLike 检索的关键字
     */
    public PagedList<SystemFeatureFormSimple> queryWithTenant(String tenant, String nameLike, long page, int limit) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return null;
        }
        SystemFeatureListTenantQuery query = new SystemFeatureListTenantQuery();
        query.setPage(page);
        query.setLimit(limit);
        query.setNameLike(nameLike);
        query.setTenantCode(tenant);
        return listForm(SystemFeatureFormSimple.class, query);
    }
    
    /**
     * 获取全部的功能清单。
     */
    public PagedList<SystemFeatureFormSimple> query(int page, int limit) throws Exception {
        return query(null, page, limit);
    }
    
    /**
     * 获取功能详情数据，包括授权信息。
     */
    public SystemFeatureFormDetail get(Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<SystemFeatureFormDetail> result;
        if (idOrCode.toString().matches("^\\d+$")) {
            result = queryByIds(SystemFeatureFormDetail.class, CommonUtil.parseLong(idOrCode));
        } else {
            result = queryByCodes(SystemFeatureFormDetail.class, idOrCode.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return withAuths(result.get(0));
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
    public List<Long> getAuthTenantFeatures(String tenant, String ...authKeys) throws Exception {
        if (authKeys == null || authKeys.length <= 0 || StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(Long.class,
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
    public List<Long> getTenantFeatures(String tenant) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(Long.class, SQL_QUERY_TENANT_FEATURES, new Object[] { tenant });
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
    public List<String> getTenantAuths(String tenant, Long... features) throws Exception {
        if (StringUtils.isBlank(tenant) || features == null || features.length <= 0) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(String.class,
                String.format("%s AND tf.feature_id IN (%s)", SQL_QUERY_TENANT_AUTHS,
                        CommonUtil.join("?", features.length, ",")),
                ArrayUtils.addAll(new Object[] { tenant }, (Object[])features));
    }

    /**
     * 获取租户的所有授权操作清单 
     */
    public List<String> getTenantAllAuths(String tenant) throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(String.class, SQL_QUERY_TENANT_AUTHS, new Object[] { tenant });
    }
    
    /**
     * 获取租户下是否拥有指定的授权操作
     */
    public boolean checkTenantAuth(String tenant, String authKey) throws Exception {
        if (StringUtils.isBlank(tenant) || StringUtils.isBlank(authKey)) {
            return false;
        }
        return getFormBaseDao().queryAsList(String.class,
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
    public <T extends SystemFeatureFormSimple> List<T> queryByIds(@NonNull Class<T> clazz, final Long... ids) throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return queryFormWithStateRevision(clazz, String.format(SQL_QUERY_ID_FEATURES, 
                         getFormTable(), CommonUtil.join("?", ids.length, ",")), ids);
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
    public <T extends SystemFeatureFormSimple> List<T> queryByIdsWithTenant(@NonNull Class<T> clazz, String tenant,
            final Long... ids) throws Exception {
        if (StringUtils.isBlank(tenant) || ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return queryFormWithStateRevision(clazz,
                        String.format(SQL_QUERY_TENANT_FEATURES_BYID, getFormTable(),
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
    public <T extends SystemFeatureFormSimple> List<T> queryByCodes(@NonNull Class<T> clazz, final String... roleCodes) throws Exception {
        if (roleCodes == null || roleCodes.length <= 0) {
            return Collections.emptyList();
        }
        return queryFormWithStateRevision(clazz, String.format(SQL_QUERY_CODE_FEATURES, 
                getFormTable(), CommonUtil.join("?", roleCodes.length, ",")), roleCodes);
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
    private SystemFeatureFormDetail withAuths(SystemFeatureFormDetail form) throws Exception {
        if (form == null) {
            return null;
        }
        if (form.getId() != null) {
            form.setAuths(Collections.emptyList());
            List<String> auths = getFormBaseDao().queryAsList(String.class, SQL_QUERY_FEATURE_AUTHS,
                    new Object[] { form.getId() });
            if (auths != null && !auths.isEmpty()) {
                form.setAuths(ClassUtil.getSingltonInstance(FieldSystemAuths.class)
                        .queryDynamicValues(auths.toArray(new String[0])));
            }
            List<Long> roles = getFormBaseDao().queryAsList(Long.class, SQL_QUERY_FEATURE_ROLES, new Object[] { form.getId() });
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
    private void setFeatureAuths(long formId, List<OptionSystemAuth> auths) throws Exception {
        if (auths == null) {
            return;
        }
        getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_feature_auth",
                        new ObjectMap().put("=feature_id", formId)));
                for (OptionSystemAuth auth : auths) {
                    if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                        continue;
                    }
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            "system_feature_auth",
                            new ObjectMap().put("feature_id", formId).put("=auth_key", auth.getOptionValue())));
                }
            }
        });
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemFeatureFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
