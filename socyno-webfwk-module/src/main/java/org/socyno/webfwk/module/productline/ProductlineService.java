package org.socyno.webfwk.module.productline;

import lombok.Getter;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.module.subsystem.SubsystemQueryAll;
import org.socyno.webfwk.module.subsystem.SubsystemFormDefault;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
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

public class ProductlineService extends
        AbstractStateFormServiceWithBaseDao<ProductlineFormDetail, ProductlineFormDefault, ProductlineFormSimple> {
    
    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionProductline.getInstance());
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionProductlineCode.getInstance());
    }
    
    @Getter
    private static final ProductlineService Instance = new ProductlineService();
    
    private ProductlineService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormTable() {
        return "productline";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "productline";
    }
    
    @Override
    public String getFormDisplay() {
        return "产品清单";
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
    }
    
    public class EventCreate extends AbstractStateCreateAction<ProductlineFormDetail, ProductlineFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String events, ProductlineFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, ProductlineFormDetail originForm, final ProductlineFormCreation form,
                final String message) throws Exception {
            AtomicLong id = new AtomicLong(0);
            ProductlineBasicUtil.ensuerNameFormatValid(form.getCode());
            ProductlineBasicUtil.ensureCodeOrNameNotExists(form.getCode(), form.getName(), null);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
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
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<ProductlineFormDetail, ProductlineFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
        public void check(String event, ProductlineFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ProductlineFormDetail originForm, final ProductlineFormEdition form,
                final String message) throws Exception {
            ProductlineBasicUtil.ensureCodeOrNameNotExists(originForm.getCode(), form.getName(), originForm.getId());
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id", originForm.getId())
                        .put("name", form.getName())
                        .put("owner_id", form.getOwner().getId())
                        .put("description", form.getDescription())
                    ));
            if (form.getSubsystems() != null) {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("productline_subsystem",
                        new ObjectMap().put("=productline_id", originForm.getId())));
                for (SubsystemFormSimple subsystem : form.getSubsystems()) {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("productline_subsystem", new ObjectMap()
                            .put("=productline_id", originForm.getId()).put("subsystem_id", subsystem.getId())));
                }
            }
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<ProductlineFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
        public void check(String event, ProductlineFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ProductlineFormDetail originForm, final StateFormBasicForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<ProductlineFormDetail, StateFormBasicForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
        public void check(String event, ProductlineFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ProductlineFormDetail originForm, final StateFormBasicForm form,
                final String message) throws Exception {
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<ProductlineFormDetail, StateFormBasicForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ProductlineOwnerChecker.class)
        public void check(String event, ProductlineFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ProductlineFormDetail originForm, final StateFormBasicForm form,
                final String message) throws Exception {
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        
        Create(EventCreate.class),
        
        Update(EventUpdate.class),
        
        Delete(EventDelete.class),
        
        Disable(EventDisable.class),
        
        Enable(EventEnable.class);
        
        private final Class<? extends AbstractStateAction<ProductlineFormDetail, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<ProductlineFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    /**
     * 
     * 产品线的负责人检查器
     */
    public class ProductlineOwnerChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object originForm) throws Exception {
            ProductlineWithOwner ownerForm = (ProductlineWithOwner) originForm;
            return ownerForm != null && ownerForm.getOwner() != null && ownerForm.getOwner().getId() != null
                    && ownerForm.getOwner().getId().equals(SessionContext.getUserId());
        }
    }
    
    /**
     * 通过产品线的编号，获取产品线清单
     */
    public <T extends ProductlineFormSimple> List<T> listByIds(Class<T> clazz, Long... productlineIds)
            throws Exception {
        if (productlineIds == null || productlineIds.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new ProductlineQueryDefault( productlineIds.length).setDisableIncluded(true)
                .setIdsIn(StringUtils.join( productlineIds, ','))).getList();
    }
    
    /**
     * 通过产品线的代码，获取产品线清单
     */
    public <T extends ProductlineFormSimple> List<T> listByCodes(Class<T> clazz, String... productlineCodes)
            throws Exception {
        if (productlineCodes == null || productlineCodes.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new ProductlineQueryDefault(productlineCodes.length).setDisableIncluded(true)
                .setCodesIn(StringUtils.join(productlineCodes, ','))).getList();
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
    public List<SubsystemFormSimple> getSubsystems(Long productlineId) throws Exception {
        return getSubsystems(new Long[] { productlineId }).get(productlineId);
    }
    
    /**
     * 获取产品线的业务系统清单(不包括已下线)
     */
    private Map<Long, List<SubsystemFormSimple>> getSubsystems(Long[] productlineIds) throws Exception {
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
        
        List<SubsystemFormDefault> subsystemAll;
        if ((subsystemAll = SubsystemService.getInstance()
                .listForm(SubsystemFormDefault.class,
                        new SubsystemQueryAll(subsyproductIds.size(), 1L).setDisableIncluded(false)
                                .setIdsIn(StringUtils.join(subsyproductIds.keySet(), ',')))
                .getList()) == null || subsystemAll.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Map<Long, List<SubsystemFormSimple>> result = new HashMap<>();
        for (SubsystemFormSimple subsystem : subsystemAll) {
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
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ProductlineFormDefault>(
                "默认查询",
                ProductlineFormDefault.class,
                ProductlineQueryDefault.class
            )),
        OPTIONS(new StateFormNamedQuery<ProductlineFormDefault>(
                "选项查询", 
                ProductlineFormDefault.class,
                ProductlineQueryOptions.class
            ));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    /**
     * 补全表单的必要详情数据
     */
    @Override
    protected void fillExtraFormFields(Collection<? extends ProductlineFormSimple> forms) throws Exception {
        
        if (forms == null || forms.size() <= 0) {
            return;
        }
        
        List<ProductlineFormSimple> singleWithExtras;
        Map<Long, List<ProductlineFormSimple>> mappedWithOwners = new HashMap<>();
        Map<Long, List<ProductlineFormSimple>> mappedWithSubsystems = new HashMap<>();
        for (ProductlineFormSimple form : forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            
            if (ProductlineWithOwner.class.isAssignableFrom(form.getClass())) {
                if((singleWithExtras = mappedWithOwners.get(form.getId())) == null) {
                    mappedWithOwners.put(form.getId(), singleWithExtras = new ArrayList<>());
                }
                singleWithExtras.add(form);
            }
            
            if (ProductlineWithSubsystems.class.isAssignableFrom(form.getClass())) {
                if((singleWithExtras = mappedWithSubsystems.get(form.getId())) == null) {
                    mappedWithSubsystems.put(form.getId(), singleWithExtras = new ArrayList<>());
                }
                singleWithExtras.add(form);
            }
        }
        
        /* 填补负责人信息 */
        if (mappedWithOwners.size() > 0) {
            Map<Long, OptionSystemUser> productlineOwners;
            if ((productlineOwners = getOwners(mappedWithOwners.keySet().toArray(new Long[0]))) != null
                    && !productlineOwners.isEmpty()) {
                for (Map.Entry<Long, List<ProductlineFormSimple>> e : mappedWithOwners.entrySet()) {
                    long productlineId = e.getKey();
                    for (ProductlineFormSimple form : e.getValue()) {
                        ((ProductlineWithOwner) form).setOwner(productlineOwners.get(productlineId));
                    }
                }
            }
        }
        
        /* 填补业务系统清单 */
        if (mappedWithSubsystems.size() > 0) {
            Map<Long, List<SubsystemFormSimple>> productlineSubsystems;
            if ((productlineSubsystems = getSubsystems(mappedWithOwners.keySet().toArray(new Long[0]))) != null
                    && !productlineSubsystems.isEmpty()) {
                for (Map.Entry<Long, List<ProductlineFormSimple>> e : mappedWithSubsystems.entrySet()) {
                    long productlineId = e.getKey();
                    for (ProductlineFormSimple form : e.getValue()) {
                        ((ProductlineWithSubsystems) form).setSubsystems(productlineSubsystems.get(productlineId));
                    }
                }
            }
        }
    }
}
