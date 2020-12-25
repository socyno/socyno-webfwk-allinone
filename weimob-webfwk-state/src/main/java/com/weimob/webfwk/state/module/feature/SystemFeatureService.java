package com.weimob.webfwk.state.module.feature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateCreateAction;
import com.weimob.webfwk.state.abs.AbstractStateDeleteAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.field.FieldSystemAuths;
import com.weimob.webfwk.state.field.OptionSystemAuth;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.state.util.StateFormEventClassEnum;
import com.weimob.webfwk.state.util.StateFormEventResultCreateViewBasic;
import com.weimob.webfwk.state.util.StateFormNamedQuery;
import com.weimob.webfwk.state.util.StateFormQueryBaseEnum;
import com.weimob.webfwk.state.util.StateFormStateBaseEnum;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.model.PagedList;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;
import com.weimob.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

public class SystemFeatureService extends
        AbstractStateFormServiceWithBaseDao<SystemFeatureFormDetail, SystemFeatureFormDefault, SystemFeatureFormSimple> {
    
    private SystemFeatureService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemFeatureService Instance = new SystemFeatureService();
    
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
    
    public class EventCreate extends AbstractStateCreateAction<SystemFeatureFormDetail, SystemFeatureFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemFeatureFormDetail originForm,
                SystemFeatureFormCreation form, String message) throws Exception {
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
            return new StateFormEventResultCreateViewBasic(id.get());
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
        public Void handle(String event, final SystemFeatureFormDetail originForm, final StateFormBasicInput form,
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
     * 检索功能清单
     */
    public PagedList<SystemFeatureFormSimple> query(String nameLike, long page, int limit) throws Exception {
        return listForm(SystemFeatureFormSimple.class,
                new SystemFeatureQueryDefault(limit, page).setNameLike(nameLike));
    }
    
    /**
     * 检索功能清单
     */
    public PagedList<SystemFeatureFormSimple> queryWithTenant(String tenant, String nameLike, long page, int limit)
            throws Exception {
        if (StringUtils.isBlank(tenant)) {
            return null;
        }
        return listForm(SystemFeatureFormSimple.class,
                new SystemFeatureQueryDefault(limit, page).setTenantCode(tenant).setNameLike(nameLike));
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
     * 通过给定的编号列表，检索系统功能清单
     */
    public <T extends SystemFeatureFormSimple> List<T> queryByIds(@NonNull Class<T> clazz, final Long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemFeatureQueryDefault(ids.length, 1L).setIdsIn(StringUtils.join(ids, ',')))
                .getList();
    }
    
    /**
     * 通过给定的编号列表，检索租户功能清单
     */
    public <T extends SystemFeatureFormSimple> List<T> queryByIdsWithTenant(@NonNull Class<T> clazz, String tenant,
            final Long... ids) throws Exception {
        if (StringUtils.isBlank(tenant) || ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemFeatureQueryDefault(ids.length, 1L).setTenantCode(tenant)
                .setIdsIn(StringUtils.join(ids, ','))).getList();
    }
    
    /**
     * 通过给定的编号列表，检索系统功能清单。
     */
    public <T extends SystemFeatureFormSimple> List<T> queryByCodes(@NonNull Class<T> clazz, final String... codes)
            throws Exception {
        if (codes == null || codes.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemFeatureQueryDefault(codes.length, 1L).setCodesIn(StringUtils.join(codes, ',')))
                .getList();
    }
    
    /**
     * 存储功能的授权数据
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
    
    @Getter
    @Setter
    @ToString
    public static class FeatureAuthKey {
        
        private long featureId;
        
        private String authKey;
        
    }
    
    /**
     * SELECT DISTINCT
     *     a.feature_id,
     *     a.auth_key
     * FROM
     *     system_feature_auth a
     * WHERE
     *     a.feature_id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_FEATURE_AUTHS = "X";
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemFeatureFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        Map<Long, List<SystemFeatureFormSimple>> mappedWithAuths = new HashMap<>();
        for (SystemFeatureFormSimple form : forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            List<SystemFeatureFormSimple> singleWiths;
            if (SystemFeatureWithAuths.class.isAssignableFrom(form.getClass())) {
                if ((singleWiths = mappedWithAuths.get(form.getId())) == null) {
                    mappedWithAuths.put(form.getId(), singleWiths = new ArrayList<>());
                }
                singleWiths.add(form);
            }
        }
        
        if (mappedWithAuths.size() > 0) {
			List<FeatureAuthKey> flattedFeatureAuths = getFormBaseDao().queryAsList(FeatureAuthKey.class,
					String.format(SQL_QUERY_FEATURE_AUTHS, CommonUtil.join("?", mappedWithAuths.size(), ",")),
					mappedWithAuths.keySet().toArray());
			if (flattedFeatureAuths != null && flattedFeatureAuths.size() > 0) {
                Set<String> singleFeatureAuths;
                Set<String> flattedFeatureAuthKeys = new HashSet<>();
                Map<Long, Set<String>> mappedFeatureAuthKeys = new HashMap<>();
                for (FeatureAuthKey auth : flattedFeatureAuths) {
                    if ((singleFeatureAuths = mappedFeatureAuthKeys.get(auth.getFeatureId())) == null) {
                        mappedFeatureAuthKeys.put(auth.getFeatureId(), singleFeatureAuths = new HashSet<>());
                    }
                    singleFeatureAuths.add(auth.getAuthKey());
                    flattedFeatureAuthKeys.add(auth.getAuthKey());
                }
                Map<String, OptionSystemAuth> mappedSystemAuths = new HashMap<>();
                List<OptionSystemAuth> flattedSystemAuths = ClassUtil.getSingltonInstance(FieldSystemAuths.class)
                        .queryDynamicValues(flattedFeatureAuthKeys.toArray(new String[0]));
                for (OptionSystemAuth auth : flattedSystemAuths) {
                    mappedSystemAuths.put(auth.getAuth(), auth);
                }
                for (Map.Entry<Long, List<SystemFeatureFormSimple>> e : mappedWithAuths.entrySet()) {
                    long formId = e.getKey();
                    if (mappedFeatureAuthKeys.get(formId) == null) {
                        continue;
                    }
                    Set<OptionSystemAuth> featuredSystemAuths = new HashSet<>();
                    for (String authKey : mappedFeatureAuthKeys.get(formId)) {
                        if (mappedSystemAuths.containsKey(authKey)) {
                            featuredSystemAuths.add(mappedSystemAuths.get(authKey));
                        }
                    }
                    for (SystemFeatureFormSimple form : e.getValue()) {
                        ((SystemFeatureWithAuths) form).setAuths(new ArrayList<>(featuredSystemAuths));
                    }
                }
            }
        }
    }
}
