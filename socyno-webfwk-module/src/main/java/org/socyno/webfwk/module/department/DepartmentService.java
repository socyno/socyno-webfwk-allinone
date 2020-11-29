package org.socyno.webfwk.module.department;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import lombok.Getter;

import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;
import org.socyno.webfwk.module.subsystem.SubsystemListAllQuery;
import org.socyno.webfwk.module.subsystem.SubsystemListDefaultForm;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DepartmentService extends AbstractStateFormServiceWithBaseDao<DepartmentFormDetail> {
    
    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionDepartment.getInstance());
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionDepartmentCode.getInstance());
    }
    
    public static final DepartmentService DEFAULT = new DepartmentService();
    
    @Override
    public String getFormTable() {
        return "productline";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    public String getFormName() {
        return "productline";
    }
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    
    @Override
    protected Map<String, AbstractStateAction<DepartmentFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<DepartmentFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "有效")
        , DISABLED("disabled", "禁用")
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
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        
        Create(new AbstractStateSubmitAction<DepartmentFormDetail, DepartmentFormForCreate>("添加",
                STATES.ENABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String events, DepartmentFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, DepartmentFormDetail originForm, final DepartmentFormForCreate form,
                    final String message) throws Exception {
                AtomicLong id = new AtomicLong(0);
                DepartmentBasicUtil.ensuerNameFormatValid(form.getCode());
                DepartmentBasicUtil.ensureCodeOrNameNotExists(form.getCode(), form.getName(), null);
                getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        DEFAULT.getFormTable(), new ObjectMap()
                            .put("code", form.getCode())
                            .put("name", form.getName())
                            .put("owner_id", form.getOwner())
                            .put("description", form.getDescription())),
                        new ResultSetProcessor() {
                            @Override
                            public void process(ResultSet resultSet, Connection connection) throws Exception {
                                resultSet.next();
                                id.set(resultSet.getLong(1));
                            }
                        });
                return id.get();
            }
        }),
        
        Update(new AbstractStateAction<DepartmentFormDetail, DepartmentFormForUpdate, Void>("编辑", STATES.stringifyEx(),
                "") {
            @Override
            @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
            public void check(String event, DepartmentFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, DepartmentFormDetail originForm, final DepartmentFormForUpdate form,
                    final String message) throws Exception {
                DepartmentBasicUtil.ensureCodeOrNameNotExists(originForm.getCode(), form.getName(), originForm.getId());
                getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                        DEFAULT.getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
                            .put("name", form.getName())
                            .put("owner_id", form.getOwner().getId())
                            .put("description", form.getDescription())
                        ));
                if (form.getSubsystems() != null) {
                    getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("productline_subsystem",
                            new ObjectMap().put("=productline_id", originForm.getId())));
                    for (SubsystemBasicForm subsystem : form.getSubsystems()) {
                        getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("productline_subsystem", new ObjectMap()
                                .put("=productline_id", originForm.getId()).put("subsystem_id", subsystem.getId())));
                    }
                }
                return null;
            }
        }),
        
        Delete(new AbstractStateDeleteAction<DepartmentFormDetail>("删除", STATES.stringifyEx()) {
            @Override
            @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
            public void check(String event, DepartmentFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, DepartmentFormDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(DEFAULT.getFormTable(),
                        new ObjectMap().put("=id", originForm.getId())));
                return null;
            }
        }),
        
        Disable(new AbstractStateAction<DepartmentFormDetail, BasicStateForm, Void>("禁用",
                STATES.stringifyEx(STATES.DISABLED), STATES.DISABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
            public void check(String event, DepartmentFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, DepartmentFormDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                return null;
            }
        }),
        
        Enable(new AbstractStateAction<DepartmentFormDetail, BasicStateForm, Void>("启用",
                STATES.stringifyEx(STATES.ENABLED), STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
            public void check(String event, DepartmentFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, DepartmentFormDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                return null;
            }
        });
        
        private final AbstractStateAction<DepartmentFormDetail, ?, ?> action;
        
        EVENTS(AbstractStateAction<DepartmentFormDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<DepartmentFormDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    /**
     * 
     * 产品线的负责人检查器
     */
    public static class ProductlineOwnerChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object originForm) throws Exception {
            DepartmentWithOwner ownerForm = (DepartmentWithOwner) originForm;
            return ownerForm != null && ownerForm.getOwner() != null && ownerForm.getOwner().getId() != null
                    && ownerForm.getOwner().getId().equals(SessionContext.getUserId());
        }
    }
    
    /**
     * 通过产品线的编号，获取详情数据
     */
    @Override
    public DepartmentFormDetail getForm(long formId) throws Exception {
        return get(DepartmentFormDetail.class, formId);
    }
    
    /**
     * 通过产品线的编号或代码，获取表单数据
     */
    public <T extends DepartmentAbstractForm> T get(Class<T> clazz, Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<T> list = idOrCode.toString().matches("^\\d+$") 
                    ? list(clazz, Long.valueOf(idOrCode.toString()))
                    : list(clazz, new String[] { idOrCode.toString() });
        if (list == null || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 通过产品线的编号，获取产品线清单
     */
    public <T extends DepartmentAbstractForm> List<T> list(Class<T> clazz, Long... productlineIds) throws Exception {
        if (productlineIds == null || productlineIds.length <= 0) {
            return Collections.emptyList();
        }
        List<T> list = queryFormWithStateRevision(clazz,
                String.format("%s WHERE s.id IN (%s) ",
                        String.format(DepartmentListDefaultQuery.SQL_SELECT_FORM, getFormTable()),
                        CommonUtil.join("?", productlineIds.length, ",")),
                productlineIds);
        if (list == null || list.size() != 1) {
            Collections.emptyList();
        }
        fillFormDetails(clazz, list);
        return list;
    }
    
    /**
     * 通过产品线的代码，获取产品线清单
     */
    public <T extends DepartmentAbstractForm> List<T> list(Class<T> clazz, String... productlineCodes) throws Exception {
        if (productlineCodes == null || productlineCodes.length <= 0) {
            return Collections.emptyList();
        }
        List<T> list = queryFormWithStateRevision(clazz,
                String.format("%s WHERE s.code IN (%s) ",
                        String.format(DepartmentListDefaultQuery.SQL_SELECT_FORM, getFormTable()),
                        CommonUtil.join("?", productlineCodes.length, ",")),
                productlineCodes);
        if (list == null || list.size() != 1) {
            Collections.emptyList();
        }
        fillFormDetails(clazz, list);
        return list;
    }
    
    /**
     * 补全表单的必要详情数据
     */
    <T extends AbstractStateForm> void fillFormDetails(Class<T> itemClazz, List<T> resultSet) throws Exception {
        if (resultSet == null || resultSet.size() <= 0) {
            return;
        }
        Map<Long, AbstractStateForm> mapResultSet = new HashMap<>();
        for (T r : resultSet) {
            mapResultSet.put(r.getId(), r);
        }
        
        /* 填补负责人信息 */
        if (DepartmentWithOwner.class.isAssignableFrom(itemClazz)) {
            Map<Long, OptionSystemUser> productlineOwners;
            if ((productlineOwners = getOwners(mapResultSet.keySet().toArray(new Long[0]))) != null
                    && !productlineOwners.isEmpty()) {
                for (Map.Entry<Long, OptionSystemUser> e : productlineOwners.entrySet()) {
                    ((DepartmentWithOwner) mapResultSet.get(e.getKey())).setOwner(e.getValue());
                }
            }
        }
        
        /* 填补业务系统清单 */
        if (DepartmentWithSubsystems.class.isAssignableFrom(itemClazz)) {
            Map<Long, List<SubsystemBasicForm>> productlineSubsystems;
            if ((productlineSubsystems = getSubsystems(mapResultSet.keySet().toArray(new Long[0]))) != null
                    && !productlineSubsystems.isEmpty()) {
                for (Map.Entry<Long, List<SubsystemBasicForm>> e : productlineSubsystems.entrySet()) {
                    ((DepartmentWithSubsystems) mapResultSet.get(e.getKey())).setSubsystems(e.getValue());
                }
            }
        }
    }
    
    /**
     * 获取产品线的负责人
     */
    public OptionSystemUser getOwner(Long prouctlineId) throws Exception {
        if (prouctlineId == null) {
            return null;
        }
        return getOwners(new Long[] { prouctlineId }).get(prouctlineId);
    }
    
    /**
     * 获取产品线的负责人
     */
    public Map<Long, OptionSystemUser> getOwners(Long... prouctlineIds) throws Exception {
        if (prouctlineIds == null || prouctlineIds.length <= 0) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> productOwners = getFormBaseDao()
                .queryAsList(String.format("SELECT id, owner_id FROM %s WHERE id IN (%s) AND owner_id IS NOT NULL",
                        getFormTable(), CommonUtil.join("?", prouctlineIds.length, ",")), prouctlineIds);
        if (productOwners == null || productOwners.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, Long> productUsers = new HashMap<>();
        for (Map<String, Object> productOwner : productOwners) {
            productUsers.put((Long) productOwner.get("id"), (Long) productOwner.get("owner_id"));
        }
        List<OptionSystemUser> allOptionUsers;
        if ((allOptionUsers = ClassUtil.getSingltonInstance(FieldSystemUser.class)
                .queryDynamicValues(productUsers.values().toArray(new Long[0]))) == null
                || allOptionUsers.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, OptionSystemUser> mapOptionUsers = new HashMap<>();
        for (OptionSystemUser aou : allOptionUsers) {
            mapOptionUsers.put(aou.getId(), aou);
        }
        
        Map<Long, OptionSystemUser> result = new HashMap<>();
        for (Map.Entry<Long, Long> e : productUsers.entrySet()) {
            result.put(e.getKey(), mapOptionUsers.get(e.getValue()));
        }
        return result;
    }
    
    /**
     * 获取产品线的业务系统清单
     */
    List<SubsystemBasicForm> getSubsystems(Long productlineId) throws Exception {
        return getSubsystems(new Object[] {productlineId}).get(productlineId);
    }
    
    /**
     * 获取产品线的业务系统清单(不包括已下线)
     */
    private Map<Long, List<SubsystemBasicForm>> getSubsystems(Object[] productlineIds) throws Exception {
        if (productlineIds == null || productlineIds.length <= 0) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> prodsubsysIds = getFormBaseDao().queryAsList(String.format(
                "SELECT DISTINCT productline_id, subsystem_id FROM productline_subsystem WHERE productline_id IN (%s)",
                CommonUtil.join("?", productlineIds.length, ",")), productlineIds);
        if (prodsubsysIds == null || prodsubsysIds.size() <= 0) {
            return Collections.emptyMap();
        }
        Long subsystemId;
        Map<Long, Set<Long>> subsyproductIds = new HashMap<>();
        for (Map<String, Object> prodsubsysId : prodsubsysIds) {
            subsystemId = (Long) prodsubsysId.get("subsystem_id");
            if (!subsyproductIds.containsKey(subsystemId)) {
                subsyproductIds.put(subsystemId, new HashSet<>());
            }
            subsyproductIds.get(subsystemId).add((Long) prodsubsysId.get("productline_id"));
        }
        
        List<SubsystemListDefaultForm> subsystemAll;
        if ((subsystemAll = SubsystemService.DEFAULT.list(SubsystemListDefaultForm.class,
                new SubsystemListAllQuery(subsyproductIds.size(), 1L).setDisableIncluded(false)
                        .setIdsIn(StringUtils.join(subsyproductIds.keySet(), ',')))
                .getList()) == null || subsystemAll.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Map<Long, List<SubsystemBasicForm>> result = new HashMap<>();
        for (SubsystemBasicForm subsystem : subsystemAll) {
            for (long productlineId : subsyproductIds.get(subsystem.getId())) {
                if (!result.containsKey(productlineId)) {
                    result.put(productlineId, new ArrayList<>());
                }
                result.get(productlineId).add(subsystem);
            }
        }
        return result;
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<DepartmentListDefaultForm>("default", DepartmentListDefaultForm.class,
                DepartmentListDefaultQuery.class)),
        OPTIONS(new StateFormNamedQuery<DepartmentBasicForm>("options", DepartmentBasicForm.class,
                DepartmentListDefaultQueryForOptions.class)),
        DETAILS(new StateFormNamedQuery<DepartmentFormDetail>("details", DepartmentFormDetail.class,
                DepartmentListDefaultQuery.class));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (DepartmentService.QUERIES item : DepartmentService.QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
}
