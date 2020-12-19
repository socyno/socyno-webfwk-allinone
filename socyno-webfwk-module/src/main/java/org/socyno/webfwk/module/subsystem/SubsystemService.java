package org.socyno.webfwk.module.subsystem;

import lombok.Getter;

import org.socyno.webfwk.module.application.*;
import org.socyno.webfwk.module.application.FieldApplication.OptionApplication;
import org.socyno.webfwk.module.productline.FieldProductline;
import org.socyno.webfwk.module.productline.FieldProductline.OptionProductline;
import org.socyno.webfwk.module.subsystem.SubsystemApplicationSummary.AppTypeSummary;
import org.socyno.webfwk.module.systenant.AbstractSystemTenant;
import org.socyno.webfwk.module.systenant.SystemTenantService;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedService;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedSubsystemInfo;
import org.socyno.webfwk.modutil.SubsystemBasicUtil;
import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.AbstractUser;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SubsystemService
        extends AbstractStateFormServiceWithBaseDao<SubsystemFormDetail, SubsystemFormDefault, SubsystemFormSimple> {
    
    @Getter
    private static final SubsystemService Instance = new SubsystemService();
    
    private SubsystemService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionSubsystem.getInstance());
    }
    
    @Override
    public String getFormTable() {
        return SubsystemBasicUtil.getFormTableName();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SubsystemBasicUtil.getFormBaseDao();
    }
    
    @Override
    public String getFormName() {
        return getFormTable();
    }
    
    @Override
    public String getFormDisplay() {
        return "业务系统";
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
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
    
    public class EventCreate extends AbstractStateCreateAction<SubsystemFormDetail, SubsystemFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String events, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SubsystemFormDetail originForm, final SubsystemFormCreation form,
                final String message) throws Exception {
            AtomicLong id = new AtomicLong(0);
            SubsystemBasicUtil.ensuerNameFormatValid(form.getCode());
            SubsystemBasicUtil.ensureCodeOrNameNotExists(form.getCode(), form.getName(), null);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(), new ObjectMap()
                    .put("code", form.getCode()).put("name", form.getName()).put("description", form.getDescription())),
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
    
    public class EventUpdate extends AbstractStateAction<SubsystemFormDetail, SubsystemFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final SubsystemFormEdition form,
                final String message) throws Exception {
            SubsystemBasicUtil.ensureCodeOrNameNotExists(originForm.getCode(), form.getName(), originForm.getId());
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())
                            .put("code", form.getCode())
                            .put("name", form.getName())
                            .put("description", form.getDescription())));
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SubsystemFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final StateFormBasicInput form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SubsystemFormDetail, StateFormBasicInput, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<SubsystemFormDetail, StateFormBasicInput, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
    }
    
    public class EventSyncCodePermission extends AbstractStateAction<SubsystemFormDetail, StateFormBasicInput, Void> {
        
        public EventSyncCodePermission() {
            super("同步代码仓授权", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public final Boolean messageRequired() {
            return true;
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final StateFormBasicInput form,
                final String message) throws Exception {
            VcsUnifiedService.CommonCloud.resetGroupMembers(originForm.getId());
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        
        Create(EventCreate.class),
        
        Update(EventUpdate.class),
        
        Delete(EventDelete.class),
        
        Disable(EventDisable.class),
        
        Enable(EventEnable.class),
        
        SyncCodePermission(EventSyncCodePermission.class)
        ;
        
        private final Class<? extends AbstractStateAction<SubsystemFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SubsystemFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class SubsystemScopeIdParser implements AuthorityScopeIdParser {
        @Override
        public String getAuthorityScopeId(Object originForm) {
            SubsystemAbstractForm form = (SubsystemAbstractForm) originForm;
            if (form == null) {
                return null;
            }
            return SubsystemBasicUtil.subsytemIdToBusinessId(form.getId());
        }
    }
    
    /**
     * 获取业务系统的负责人信息
     */
    public List<OptionSystemUser> getOwners(Long subsystemId) throws Exception {
        if (subsystemId == null) {
            return null;
        }
        return getOwners(new Long[] { subsystemId }).get(subsystemId);
    }
    
    /**
     * 获取业务系统的负责人信息
     */
    public Map<Long, List<OptionSystemUser>> getOwners(Long[] subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyMap();
        }
        Map<String, List<OptionSystemUser>> businessOwners;
        if ((businessOwners = PermissionService
                .getBusinessOwners(SubsystemBasicUtil.subsytemIdToBusinessId(subsystemIds))) == null
                || businessOwners.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Map<Long, List<OptionSystemUser>> subsystemOwners = new HashMap<>();
        for (Map.Entry<String, List<OptionSystemUser>> e : businessOwners.entrySet()) {
            subsystemOwners.put(SubsystemBasicUtil.subsytemIdFromBusinessId(e.getKey()), e.getValue());
        }
        return subsystemOwners;
    }
    
    /**
     * 获取业务系统所属产品线
     */
    public List<OptionProductline> getProductlines(Long subsystemId) throws Exception {
        if (subsystemId == null) {
            return null;
        }
        return getProductlines(new Long[] { subsystemId }).get(subsystemId);
    }
    
    /**
     * 获取业务系统所属产品线
     */
    Map<Long, List<OptionProductline>> getProductlines(Long[] subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> prodsubsysIds = getFormBaseDao().queryAsList(String.format(
                "SELECT DISTINCT productline_id, subsystem_id FROM productline_subsystem WHERE subsystem_id IN (%s)",
                CommonUtil.join("?", subsystemIds.length, ",")), subsystemIds);
        if (prodsubsysIds == null || prodsubsysIds.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Long productlineId;
        Set<Long> productlineIdsAll = new HashSet<>();
        for (Map<String, Object> prodsubsysId : prodsubsysIds) {
            productlineId = (Long) prodsubsysId.get("productline_id");
            productlineIdsAll.add(productlineId);
        }
        
        List<OptionProductline> productlineAll;
        if ((productlineAll = ClassUtil.getSingltonInstance(FieldProductline.class)
                .queryDynamicValues(productlineIdsAll.toArray(new Long[0]))) == null || productlineAll.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Long subsystemId;
        Map<Long, List<OptionProductline>> result = new HashMap<>();
        for (OptionProductline productline : productlineAll) {
            for (Map<String, Object> prodsubsysId : prodsubsysIds) {
                if (!productline.getId().equals((Long) prodsubsysId.get("productline_id"))) {
                    continue;
                }
                subsystemId = (Long) prodsubsysId.get("subsystem_id");
                if (!result.containsKey(subsystemId)) {
                    result.put(subsystemId, new ArrayList<>());
                }
                result.get(subsystemId).add(productline);
            }
        }
        
        return result;
    }
    
    /**
     * 通过业务系统的代码，获取清单
     */
    private <T extends SubsystemFormSimple> List<T> listByCodes(Class<T> clazz, boolean disableIncluded,
            String... subsystemCodes) throws Exception {
        if (subsystemCodes == null || subsystemCodes.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SubsystemQueryAll(subsystemCodes.length, 1L).setDisableIncluded(disableIncluded)
                .setCodesIn(StringUtils.join(subsystemCodes, ','))).getList();
    }
    
    public <T extends SubsystemFormSimple> List<T> listEnabledByCodes(Class<T> clazz, String... subsystemCodes) throws Exception {
        return listByCodes(clazz, false, subsystemCodes);
    }
    
    public <T extends SubsystemFormSimple> List<T> listAllByCodes(Class<T> clazz, String... subsystemCodes) throws Exception {
        return listByCodes(clazz, true, subsystemCodes);
    }
    
    public <T extends SubsystemFormSimple> T getByCode(Class<T> clazz, String subsystemCode) throws Exception {
        List<T> subsys;
        if ((subsys = listByCodes(clazz, true, subsystemCode)) == null || subsys.size() <= 0) {
            return null;
        }
        return subsys.get(0);
    }
    
    /**
     * 获取业务系统的代码可访问人员清单(包括代码下载、变更和仓库管理)
     */
    public List<? extends AbstractUser> collectAllCodePermGroupMembers(long subsystemId) throws Exception {
        List<Long> members = PermissionService.queryBusinessUsersByAuthKey(
                SubsystemBasicUtil.subsytemIdToBusinessId(subsystemId), true,
                new String[] { ApplicationService.getInstance().getCodeAccessFormEventKey(),
                        ApplicationService.getInstance().getCodePushFormEventKey(),
                        ApplicationService.getInstance().getCodeMaintainerFormEventKey() });
        return ClassUtil.getSingltonInstance(FieldSystemUser.class).queryDynamicValues(members.toArray(new Long[0]));
    }
    
    /**
     * 获取业务系统的源码仓库权限信息
     */
    public VcsUnifiedSubsystemInfo getCodePermGroupNameWithNamespace(long subsystemId) throws Exception {
        AbstractSystemTenant tenant;
        if ((tenant = SystemTenantService.getInstance().getSimple(SessionContext.getTenant())) == null
                || StringUtils.isBlank(tenant.getCodeNamespace())) {
            throw new MessageException("获取租户代码空间未设置");
        }
        SubsystemFormSimple subsystem;
        if ((subsystem = getForm(SubsystemFormSimple.class, subsystemId)) == null) {
            throw new MessageException("业务系统不存在,或已被删除");
        }
        return new VcsUnifiedSubsystemInfo(tenant.getCodeNamespace(), subsystem.getCode());
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        ACCSESSOR(new StateFormNamedQuery<SubsystemFormDefault>("accessor", SubsystemFormDefault.class,
                SubsystemQueryAccessable.class));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (SubsystemService.QUERIES item : SubsystemService.QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SubsystemFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        List<SubsystemFormSimple> sameMappedItems;
        Map<Long, List<SubsystemFormSimple>> mappedWithOwners = new HashMap<>();
        Map<Long, List<SubsystemFormSimple>> mappedWithProductlines = new HashMap<>();
        Map<Long, List<SubsystemFormSimple>> mappedWithApplications = new HashMap<>();
        Map<Long, List<SubsystemFormSimple>> mappedWithAppsummaries = new HashMap<>();
        for (SubsystemFormSimple form : forms) {
            if (SubsystemWithOwners.class.isAssignableFrom(form.getClass())) {
                if ((sameMappedItems = mappedWithOwners.get(form.getId())) == null) {
                    mappedWithOwners.put(form.getId(), sameMappedItems = new ArrayList<>());
                }
                sameMappedItems.add(form);
            }
            
            if (SubsystemWithProductlines.class.isAssignableFrom(form.getClass())) {
                if ((sameMappedItems = mappedWithProductlines.get(form.getId())) == null) {
                    mappedWithProductlines.put(form.getId(), sameMappedItems = new ArrayList<>());
                }
                sameMappedItems.add(form);
            }
            
            if (SubsystemWithApplications.class.isAssignableFrom(form.getClass())) {
                if ((sameMappedItems = mappedWithApplications.get(form.getId())) == null) {
                    mappedWithApplications.put(form.getId(), sameMappedItems = new ArrayList<>());
                }
                sameMappedItems.add(form);
            }
            
            if (SubsystemWithAppSummary.class.isAssignableFrom(form.getClass())) {
                if ((sameMappedItems = mappedWithAppsummaries.get(form.getId())) == null) {
                    mappedWithAppsummaries.put(form.getId(), sameMappedItems = new ArrayList<>());
                }
                sameMappedItems.add(form);
            }
        }
        
        /* 补全负责人数据 */
        if (mappedWithOwners.size() > 0) {
            Map<Long, List<OptionSystemUser>> mappedSubsystemOwners;
            if ((mappedSubsystemOwners = getOwners(mappedWithOwners.keySet().toArray(new Long[0]))) != null
                    && !mappedSubsystemOwners.isEmpty()) {
                for (Map.Entry<Long, List<SubsystemFormSimple>> e : mappedWithOwners.entrySet()) {
                    for (SubsystemFormSimple app : e.getValue()) {
                        ((SubsystemWithOwners) app).setOwners(mappedSubsystemOwners.get(e.getKey()));
                    }
                }
            }
        }
        
        /* 补全产品线数据 */
        if (mappedWithProductlines.size() > 0) {
            Map<Long, List<OptionProductline>> mappedSubsystemProductlines;
            if ((mappedSubsystemProductlines = getProductlines(mappedWithProductlines.keySet().toArray(new Long[0]))) != null
                    && !mappedSubsystemProductlines.isEmpty()) {
                for (Map.Entry<Long, List<SubsystemFormSimple>> e : mappedWithProductlines.entrySet()) {
                    for (SubsystemFormSimple app : e.getValue()) {
                        ((SubsystemWithProductlines) app).setProductlines(mappedSubsystemProductlines.get(e.getKey()));
                    }
                }
            }
        }
        
        Map<Long, List<OptionApplication>> mappedSubsystemApps = null;
        /* 补全应用清单数据 */
        if (mappedWithApplications.size() > 0) {
            mappedSubsystemApps = getMappedSubsystemApplications(mappedWithApplications.keySet().toArray(new Long[0]));
            for (Map.Entry<Long, List<SubsystemFormSimple>> e : mappedWithApplications.entrySet()) {
                for (SubsystemFormSimple app : e.getValue()) {
                    ((SubsystemWithApplications) app).setApplications(mappedSubsystemApps.get(e.getKey()));
                }
            }
        }
        
        /* 补全部署资源统计数据 */
        if (mappedWithAppsummaries.size() >= 0) {
            mappedSubsystemApps = CommonUtil.ifNull(mappedSubsystemApps,
                    getMappedSubsystemApplications(mappedWithApplications.keySet().toArray(new Long[0])));
            /**
             * 统计各业务系统下应用总数及各类型的数量
             */
            AtomicInteger singleSubsystemOneStateCounts;
            Map<String, AtomicInteger> singleSubsystemTypeMappedCounts;
            Map<String, Map<String, AtomicInteger>> singleSubsystemTypeStateCounts;
            Map<Long, Long> mappedAppSubsystemIds = new HashMap<>();
            Map<Long, AtomicInteger> mappedSubsystemAllCounts = new HashMap<>();
            Map<Long, Map<String, Map<String, AtomicInteger>>> mappedSubsystemTypeStateCounts = new HashMap<>();
            for (Map.Entry<Long, List<OptionApplication>> e : mappedSubsystemApps.entrySet()) {
                Long subsystemId = e.getKey();
                for (OptionApplication app : e.getValue()) {
                    String type = app.getType();
                    String state = app.getState();
                    String stateDisplay = ApplicationService.getInstance().getStateDisplay(state);
                    if ((singleSubsystemTypeStateCounts = mappedSubsystemTypeStateCounts.get(subsystemId)) == null) {
                        mappedSubsystemTypeStateCounts.put(subsystemId,
                                singleSubsystemTypeStateCounts = new HashMap<>());
                    }
                    if ((singleSubsystemTypeMappedCounts = singleSubsystemTypeStateCounts.get(type)) == null) {
                        singleSubsystemTypeStateCounts.put(type, singleSubsystemTypeMappedCounts = new HashMap<>());
                    }
                    if ((singleSubsystemOneStateCounts = singleSubsystemTypeMappedCounts.get(stateDisplay)) == null) {
                        singleSubsystemTypeMappedCounts.put(stateDisplay,
                                singleSubsystemOneStateCounts = new AtomicInteger(0));
                    }
                    singleSubsystemOneStateCounts.incrementAndGet();
                    if (mappedSubsystemAllCounts.get(subsystemId) == null) {
                        mappedSubsystemAllCounts.put(subsystemId, new AtomicInteger(0));
                    }
                    mappedAppSubsystemIds.put(app.getId(), subsystemId);
                    mappedSubsystemAllCounts.get(subsystemId).incrementAndGet();
                }
            }
            
            /**
             * 根据应用的部署空间，集群，环境概要信息，统计业务系统在各环境的概要数据
             */
            Map<Long, Map<String, DeployEnvNamespaceSummaryDetail>> mappedSubsystemAllNamespaceSummaries = new HashMap<>();
            List<FieldApplicationNamespace.OptionApplicationNamespace> allAppNamespaceOptions = FieldApplicationNamespace
                    .queryByApplications(mappedAppSubsystemIds.keySet().toArray(new Long[0]));
            if (allAppNamespaceOptions != null && !allAppNamespaceOptions.isEmpty()) {
                List<? extends DeployEnvNamespaceSummaryDetail> flatAppNamespaceSummaries;
                if ((flatAppNamespaceSummaries = ApplicationService.genApplicationNamespaceSummary(allAppNamespaceOptions)) != null) {
                    for (DeployEnvNamespaceSummaryDetail appsummary : flatAppNamespaceSummaries) {
                        long applicationId = appsummary.getTargetScopeId();
                        long subsystemId = mappedAppSubsystemIds.get(applicationId);
                        String deployEnvironment = appsummary.getEnvName();
                        Map<String, DeployEnvNamespaceSummaryDetail> mappedSubsystemEnvNamespaceSummaries;
                        if ((mappedSubsystemEnvNamespaceSummaries = mappedSubsystemAllNamespaceSummaries.get(subsystemId)) == null) {
                            mappedSubsystemAllNamespaceSummaries.put(subsystemId, mappedSubsystemEnvNamespaceSummaries = new HashMap<>());
                        }
                        DeployEnvNamespaceSummaryDetail singleSubystemEnvNamespaceSummary;
                        if ((singleSubystemEnvNamespaceSummary = mappedSubsystemEnvNamespaceSummaries.get(deployEnvironment)) == null) {
                            mappedSubsystemEnvNamespaceSummaries.put(deployEnvironment, singleSubystemEnvNamespaceSummary = new DeployEnvNamespaceSummaryDetail());
                        }
                        singleSubystemEnvNamespaceSummary.setTargetScopeId(subsystemId);
                        singleSubystemEnvNamespaceSummary.setEnvName(deployEnvironment);
                        singleSubystemEnvNamespaceSummary.setEnvDisplay(appsummary.getEnvDisplay());
                        singleSubystemEnvNamespaceSummary.addReplicas(appsummary.getReplicas());
                        singleSubystemEnvNamespaceSummary.addAppId(appsummary.getAppIds());
                        singleSubystemEnvNamespaceSummary.addClusterInfo(appsummary.getClusters());
                        singleSubystemEnvNamespaceSummary.addNamespaceInfo(appsummary.getNamespaces());
                    }
                }
            }
            
            /**
             * 整合上述业务系统的应用和部署概要信息，补全各业务系统的概要数据
             */
            Map<Long, SubsystemApplicationSummary> mappedSubsystemApplicationSummaries = new HashMap<>();
            for (Map.Entry<Long, Map<String, Map<String, AtomicInteger>>> sysEntry : mappedSubsystemTypeStateCounts
                    .entrySet()) {
                long subsystemId = sysEntry.getKey();
                AtomicInteger subsystemAppCount = new AtomicInteger(0);
                SubsystemApplicationSummary subsystemSummary = new SubsystemApplicationSummary();
                for (Map.Entry<String, Map<String, AtomicInteger>> typeEntry : sysEntry.getValue().entrySet()) {
                    String type = typeEntry.getKey();
                    AtomicInteger typedAppCount = new AtomicInteger(0);
                    AppTypeSummary typedSummary = new AppTypeSummary().setType(type).setDisplay(type);
                    for (Map.Entry<String, AtomicInteger> stateEntry : typeEntry.getValue().entrySet()) {
                        String stateName = stateEntry.getKey();
                        int stateAppCount = stateEntry.getValue().get();
                        typedAppCount.addAndGet(stateAppCount);
                        subsystemAppCount.addAndGet(stateAppCount);
                        typedSummary.addStateSummary(stateName,
                                ApplicationService.getInstance().getStateDisplay(stateName), stateAppCount);
                    }
                    typedSummary.setTotal(typedAppCount.get());
                    subsystemSummary.getTypes().add(typedSummary);
                }
                subsystemSummary.setTotal(subsystemAppCount.get());
                subsystemSummary.getEnvs().addAll(DeployEnvNamespaceSummaryDetail
                        .toSimple(mappedSubsystemAllNamespaceSummaries.get(subsystemId).values()));
                mappedSubsystemApplicationSummaries.put(subsystemId, subsystemSummary);
                
            }
            for (Map.Entry<Long, List<SubsystemFormSimple>> e : mappedWithAppsummaries.entrySet()) {
                for (SubsystemFormSimple sys : e.getValue()) {
                    ((SubsystemWithAppSummary) sys)
                            .setAppSummary(mappedSubsystemApplicationSummaries.get(e.getKey()));
                }
            }
        }
    }
    
    private Map<Long, List<OptionApplication>> getMappedSubsystemApplications(Long ...substsytemIds) throws Exception {
        Map<Long, List<OptionApplication>> mappedSubsystemApps = new HashMap<>();
        List<OptionApplication> allSubsystemApps = FieldApplication.queryValuesBySubsystemIds(
                OptionApplication.class, true, substsytemIds);
        List<OptionApplication> sameSubsystemApps;
        for (OptionApplication app : allSubsystemApps) {
            if ((sameSubsystemApps = mappedSubsystemApps.get(app.getSubsystem().getId())) == null) {
                mappedSubsystemApps.put(app.getSubsystem().getId(), sameSubsystemApps = new ArrayList<>());
            }
            sameSubsystemApps.add(app);
        }
        return mappedSubsystemApps;
    }
}
