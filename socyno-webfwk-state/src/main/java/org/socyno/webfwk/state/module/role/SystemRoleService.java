package org.socyno.webfwk.state.module.role;

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
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateDeleteAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.FieldSystemFeatureWithTenant;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NamingConflictedException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

public class SystemRoleService extends AbstractStateFormServiceWithBaseDao<SystemRoleFormDetail, SystemRoleFormDefault, SystemRoleFormSimple> {
    
    private SystemRoleService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemRoleService Instance = new SystemRoleService();
    
    @Getter
    public static enum InternalRoles {
        Admin("admin"),
        Owner("owner"),
        Basic("basic");
        
        private final String code;
        InternalRoles(String code) {
            this.code = code;
        }
        
        public static boolean contains(String code) {
            if (code != null && (code = code.trim()).isEmpty()) {
                return false;
            }
            for (InternalRoles v : InternalRoles.values()) {
                if (v.getCode().equalsIgnoreCase(code)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Update(EventEdit.class)
        , Delete(EventDelete.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemRoleFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemRoleFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemRoleFormDefault>("默认查询", 
                SystemRoleFormDefault.class, SystemRoleQueryDefault.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        Enabled("enabled", "有效")
        , Disabled("disabled", "禁用")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemRoleFormSimple, SystemRoleFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.Enabled.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleFormSimple form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemRoleFormSimple originForm,
                SystemRoleFormCreation role, String message) throws Exception {
            /* 添加角色及其授权信息 */
            ensureRoleSaveFormValid(role);
            final AtomicLong id = new AtomicLong();
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareInsertQuery(getFormTable(),
                                    new ObjectMap().put("code", role.getCode()).put("name", role.getName())
                                            .put("description", CommonUtil.ifNull(role.getDescription(), ""))),
                            new ResultSetProcessor() {
                                @Override
                                public void process(ResultSet r, Connection c) throws Exception {
                                    r.next();
                                    id.set(r.getLong(1));
                                    List<OptionSystemFeature> features;
                                    if ((features = role.getFeatures()) != null) {
                                        setFeatures(id.get(), features);
                                    }
                                }
                            });
                }
            });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemRoleFormSimple> {
        
        public EventDelete () {
            super("删除", STATES.Disabled.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemRoleFormSimple originForm, StateFormBasicInput form, String message)
                        throws Exception {
            if (InternalRoles.contains(originForm.getCode())) {
                throw new MessageException("系统内建角色，禁止删除");
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            setFeatures(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemRoleFormSimple, SystemRoleFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemRoleFormSimple originForm, final SystemRoleFormEdition role,
                final String message) throws Exception {
            /* 系统角色，不可修改其代码 */
            if (InternalRoles.contains(originForm.getCode())) {
                role.setCode(originForm.getCode());
            }
            /* 更新角色及其授权信息 */
            ensureRoleSaveFormValid(role);
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                            new ObjectMap().put("=id", role.getId()).put("code", role.getCode())
                                    .put("name", role.getName()).put("description", role.getDescription())));
                    List<OptionSystemFeature> features;
                    if ((features = role.getFeatures()) != null) {
                        setFeatures(role.getId(), features);
                    }
                }
            });
            return null;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_role";
    }
    
    @Override
    public String getFormTable() {
        return "system_role";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统角色";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    private void ensureRoleSaveFormValid(@NonNull SystemRoleSaved role) throws Exception {
        /**
         * 检查命名规范
         */
        if (StringUtils.isBlank(role.getCode()) || !role.getCode().matches("^[a-zA-Z\\_\\-\\d]+$")
                || role.getCode().matches("^\\d+$")) {
            throw new NamingFormatInvalidException("角色代码的命名规范");
        }
        
        /**
         * 检测角色代码是否已被使用
         */
        if (role.getId() == null && InternalRoles.contains(role.getCode())) {
            throw new NamingConflictedException("角色代码已被占用");
        }
        String checkSql = String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable());
        if (role.getId() != null) {
            checkSql += String.format(" AND id != %s", role.getId());
        }
        if (getFormBaseDao().queryAsObject(Long.class, String.format(checkSql, getFormTable()),
                new Object[] { role.getCode() }) != 0) {
            throw new NamingConflictedException("角色代码已被占用");
        }
    }
    
    /**
     * 检索角色清单
     */
    public PagedList<SystemRoleFormSimple> query(String nameLike, long page, int limit) throws Exception {
        return listForm(SystemRoleFormSimple.class, new SystemRoleQueryDefault(nameLike, page, limit));
    }
    
    /**
     * 获取角色详情
     */
    public SystemRoleFormSimple getSimple(Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<SystemRoleFormSimple> result;
        if (idOrCode.toString().matches("^\\d+$")) {
            result = queryByIds(SystemRoleFormSimple.class, CommonUtil.parseLong(idOrCode));
        } else {
            result = queryByCodes(SystemRoleFormSimple.class, idOrCode.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }
    
    /**
     * 通过给定的编号列表，检索角色清单
     */
    public <T extends SystemRoleFormSimple> List<T> queryByIds(@NonNull Class<T> clazz, final Long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemRoleQueryDefault(1L, ids.length).setIdsIn(StringUtils.join(ids, ",")))
                .getList();
    }
    
    /**
     * 检索角色详情
     */
    public <T extends SystemRoleFormSimple> List<T> queryByCodes(@NonNull Class<T> clazz, final String... codes)
            throws Exception {
        if (codes == null || codes.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemRoleQueryDefault(1L, codes.length).setCodesIn(StringUtils.join(codes, ",")))
                .getList();
    }
    
    private void setFeatures(long formId, List<OptionSystemFeature> features) throws Exception {
        if (features == null) {
            return;
        }
        getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareDeleteQuery("system_role_feature", new ObjectMap().put("=role_id", formId)));
        for (OptionSystemFeature feature : features) {
            if (feature == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_role_feature",
                    new ObjectMap().put("=feature_id", feature.getId()).put("role_id", formId)));
        }
    }
    
    /**
     SELECT DISTINCT
         r.role_id,
         r.feature_id
     FROM
         system_role_feature r
     WHERE
         r.role_id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_ROLE_FEATURES = "X";
    
    @Data
    public static class RoleFeaturesEntry {
        
        private Long roleId;
        
        private Long featureId;
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemRoleFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        Map<Long, SystemRoleWithFeatures> withFeatures = new HashMap<>();
        for (SystemRoleFormSimple role : forms) {
            if (role == null) {
                continue;
            }
            if (role.getId() != null && SystemRoleWithFeatures.class.isAssignableFrom(role.getClass())) {
                withFeatures.put(role.getId(), (SystemRoleWithFeatures)role);
            }
        }
        if (withFeatures.size() > 0) {
            List<RoleFeaturesEntry> features = getFormBaseDao().queryAsList(RoleFeaturesEntry.class,
                    String.format(SQL_QUERY_ROLE_FEATURES, StringUtils.join(withFeatures.keySet(), ',')));
            Set<Long> allFeatureIds = new HashSet<>();
            Map<Long, Set<Long>> mappedFeatureIds = new HashMap<>();
            for (RoleFeaturesEntry f : features) {
                if (f.getFeatureId() == null) {
                    continue;
                }
                Set<Long> roleFeatureIds;
                if ((roleFeatureIds = mappedFeatureIds.get(f.getRoleId())) == null) {
                    mappedFeatureIds.put(f.getRoleId(), roleFeatureIds = new HashSet<>());
                }
                roleFeatureIds.add(f.getFeatureId());
                allFeatureIds.add(f.getFeatureId());
            }
            List<OptionSystemFeature> optionFeatures;
            if (allFeatureIds.size() > 0
                    && (optionFeatures = ClassUtil.getSingltonInstance(FieldSystemFeatureWithTenant.class)
                            .queryDynamicValues(allFeatureIds.toArray())) != null) {
                Map<Long, OptionSystemFeature> mappedOptionFeatures = new HashMap<>();
                for (OptionSystemFeature option : optionFeatures) {
                    mappedOptionFeatures.put(option.getId(), option);
                }
                for (Map.Entry<Long, SystemRoleWithFeatures> entry : withFeatures.entrySet()) {
                    Long roleId = entry.getKey();
                    SystemRoleWithFeatures role = entry.getValue();
                    List<OptionSystemFeature> roleFeatures;
                    if ((roleFeatures = role.getFeatures()) == null) {
                        role.setFeatures(roleFeatures = new ArrayList<>());
                    }
                    Set<Long> roleFeatureIds;
                    OptionSystemFeature optionFeature;
                    if ((roleFeatureIds = mappedFeatureIds.get(roleId)) != null && roleFeatureIds.size() > 0) {
                        for (Long featureId : roleFeatureIds) {
                            if ((optionFeature= mappedOptionFeatures.get(featureId)) == null) {
                                continue;
                            }
                            roleFeatures.add(optionFeature);
                        }
                    }
                }
            }
        }
    }
}
