package org.socyno.webfwk.module.subsystem;

import lombok.Getter;
import lombok.NonNull;

import org.socyno.webfwk.module.app.form.*;
import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;
import org.socyno.webfwk.module.department.FieldDepartment;
import org.socyno.webfwk.module.department.FieldDepartment.OptionProductline;
import org.socyno.webfwk.module.systenant.AbstractSystemTenant;
import org.socyno.webfwk.module.systenant.SystemTenantService;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedService;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedSubsystemInfo;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.role.SystemRoleService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
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

public class SubsystemService
        extends AbstractStateFormServiceWithBaseDao<SubsystemFormDetail, SubsystemFormDefault, SubsystemFormSimple> {
    
    @Getter
    private static final SubsystemService Instance = new SubsystemService();
    
    public SubsystemService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionSubsystem.getInstance());
    }
    
    @Override
    public String getFormTable() {
        return "subsystem";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "subsystem";
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
    
    public class EventCreate extends AbstractStateSubmitAction<SubsystemFormDetail, SubsystemFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String events, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Long handle(String event, SubsystemFormDetail originForm, final SubsystemFormCreation form,
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
            return id.get();
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SubsystemFormDetail, SubsystemFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = SubsystemScopeIdParser.class)
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
        @Authority(value = AuthorityScopeType.Subsystem, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SubsystemFormDetail, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<SubsystemFormDetail, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
    }
    
    public class EventSyncCodePermission extends AbstractStateAction<SubsystemFormDetail, BasicStateForm, Void> {
        
        public EventSyncCodePermission() {
            super("同步代码仓授权", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = SubsystemScopeIdParser.class)
        public void check(String event, SubsystemFormDetail form, String sourceState) {
            
        }
        
        @Override
        public final Boolean messageRequired() {
            return true;
        }
        
        @Override
        public Void handle(String event, SubsystemFormDetail originForm, final BasicStateForm form,
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
        public Long getAuthorityScopeId(Object originForm) {
            SubsystemAbstractForm form = (SubsystemAbstractForm) originForm;
            if (form == null) {
                return null;
            }
            return form.getId();
        }
    }
    
    @Override
    public SubsystemFormDetail getForm(long formId) throws Exception {
        return get(SubsystemFormDetail.class, formId);
    }
    
    /**
     * 通过业务系统的编号或代码，查询业务系统及详情数据
     */
    public <T extends SubsystemFormSimple> T get(Class<T> clazz, Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<T> list = idOrCode.toString().matches("^\\d+$") ? list(clazz, false, Long.valueOf(idOrCode.toString()))
                : list(clazz, false, idOrCode.toString());
        if (list == null || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 补全表单的必要详情数据
     */
    <T extends AbstractStateForm> void fillFormDetails(Class<T> itemClazz, List<T> resultSet) throws Exception {
        if (resultSet == null || resultSet.size() <= 0) {
            return;
        }
        Map<Long, SubsystemAbstractForm> mappedResultSet = new HashMap<>();
        for (T r : resultSet) {
            mappedResultSet.put(((SubsystemAbstractForm)r).getId(), (SubsystemAbstractForm) r);
        }
        /* 补全负责人数据 */
        if (SubsystemWithOwners.class.isAssignableFrom(itemClazz)) {
            Map<Long, List<OptionSystemUser>> subsysOwners;
            if ((subsysOwners = getOwners(mappedResultSet.keySet().toArray(new Long[0]))) != null
                    && !subsysOwners.isEmpty()) {
                for (Map.Entry<Long, List<OptionSystemUser>> e : subsysOwners.entrySet()) {
                    ((SubsystemWithOwners)mappedResultSet.get(e.getKey())).setOwners(e.getValue());
                }
            }
        }
        /* 补全产品线数据 */
        if (SubsystemWithDepartments.class.isAssignableFrom(itemClazz)) {
            Map<Long, List<OptionProductline>> subsysProds;
            if ((subsysProds = getProductlines(mappedResultSet.keySet().toArray(new Long[0]))) != null
                    && !subsysProds.isEmpty()) {
                for (Map.Entry<Long, List<OptionProductline>> e : subsysProds.entrySet()) {
                    ((SubsystemWithDepartments)mappedResultSet.get(e.getKey())).setProductlines(e.getValue());
                }
            }
        }
        
        List<OptionApplication> allApplications = null;
        /* 补全应用清单数据 */
        if (SubsystemWithApplications.class.isAssignableFrom(itemClazz)) {
            allApplications = CommonUtil.ifNull(allApplications, FieldApplication
                    .queryValuesBySubsystemIds(OptionApplication.class, false, mappedResultSet.keySet().toArray(new Long[0])));
            List<OptionApplication> subsysApplications;
            for (OptionApplication app : allApplications) {
                if ((subsysApplications = ((SubsystemWithApplications)mappedResultSet.get(app.getSubsystemId())).getApplications()) == null) {
                    ((SubsystemWithApplications)mappedResultSet.get(app.getSubsystemId())).setApplications(subsysApplications = new ArrayList<>());
                }            subsysApplications.add(app);
            }
        }
        
        /* 补全部署资源统计数据 */
        if (SubsystemWithApplicationSummary.class.isAssignableFrom(itemClazz)) {
            allApplications = CommonUtil.ifNull(allApplications, FieldApplication
                    .queryValuesBySubsystemIds(OptionApplication.class, false, mappedResultSet.keySet().toArray(new Long[0])));
            Map<Long, SubsystemApplicationSummary> subsystemAppSummary = genApplicationSummary(allApplications);
            if (subsystemAppSummary != null && subsystemAppSummary.size() > 0) {
                for (T r : resultSet) {
                    ((SubsystemWithApplicationSummary) r).setAppSummary(subsystemAppSummary.get(r.getId()));
                }
            }
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
    
    private static Map<Long, SubsystemApplicationSummary> genApplicationSummary(List<OptionApplication> applications)
            throws Exception {
        if (applications == null) {
            return Collections.emptyMap();
        }
        
        /**
         * 按照业务系统及类型收集应用
         */
        Map<Long, Long> appSubsysIds = new HashMap<>();
        Map<Long, Map<String, Set<Long>>> subsysTypedAppIds = new HashMap<>();
        for (OptionApplication app : applications) {
            if (app.getSubsystemId() == null) {
                continue;
            }
            String type = app.getType();
            long subsysId = app.getSubsystemId();
            Map<String, Set<Long>> subsysEntry;
            if ((subsysEntry = subsysTypedAppIds.get(subsysId)) == null) {
                subsysTypedAppIds.put(subsysId, subsysEntry = new HashMap<>());
            }
            Set<Long> typeIds;
            if ((typeIds = subsysEntry.get(type)) == null) {
                subsysEntry.put(type, typeIds = new HashSet<>());
            }
            typeIds.add(app.getId());
            appSubsysIds.put(app.getId(), subsysId);
        }
        /**
         * 统计各环境部署的应用信息
         */
        Map<Long, Map<String, DeployEnvNamespaceSummaryDetail>> allEvnNamespaceSummary = new HashMap<>();
        List<FieldApplicationNamespace.OptionApplicationNamespace> namespaces = FieldApplicationNamespace
                .queryByApplications(appSubsysIds.keySet().toArray(new Long[0]));
        if (namespaces != null && !namespaces.isEmpty()) {
            List<? extends DeployEnvNamespaceSummaryDetail> deployNamespaceSummaries;
            if ((deployNamespaceSummaries = ApplicationService.genApplicationNamespaceSummary(namespaces)) != null) {
                for (DeployEnvNamespaceSummaryDetail es : deployNamespaceSummaries) {
                    
                    long subId = appSubsysIds.get(es.getTargetScopeId());
                    String envName = es.getEnvName();
                    Map<String, DeployEnvNamespaceSummaryDetail> subEvnNamespaceSummary;
                    if ((subEvnNamespaceSummary = allEvnNamespaceSummary.get(subId)) == null) {
                        allEvnNamespaceSummary.put(subId, subEvnNamespaceSummary = new HashMap<>());
                    }
                    DeployEnvNamespaceSummaryDetail oneEnvNamespaceSummary;
                    if ((oneEnvNamespaceSummary = subEvnNamespaceSummary.get(envName)) == null) {
                        subEvnNamespaceSummary.put(envName, oneEnvNamespaceSummary = new DeployEnvNamespaceSummaryDetail());
                    }
                    oneEnvNamespaceSummary.setTargetScopeId(subId);
                    oneEnvNamespaceSummary.setEnvName(envName);
                    oneEnvNamespaceSummary.setEnvDisplay(es.getEnvDisplay());
                    oneEnvNamespaceSummary.addReplicas(es.getReplicas());
                    oneEnvNamespaceSummary.addAppId(es.getAppIds());
                    oneEnvNamespaceSummary.addClusterInfo(es.getClusters());
                    oneEnvNamespaceSummary.addNamespaceInfo(es.getNamespaces());
                }
            }
        }
        
        Map<Long, SubsystemApplicationSummary> result = new HashMap<>();
        for (Long subsysId : subsysTypedAppIds.keySet()) {
            SubsystemApplicationSummary subsysAppSummary = new SubsystemApplicationSummary();
            if (allEvnNamespaceSummary.containsKey(subsysId)) {
                subsysAppSummary.getEvnNamespaceSummary().addAll(
                        DeployEnvNamespaceSummaryDetail.toSimple(allEvnNamespaceSummary.get(subsysId).values()));
            }
            for (Map.Entry<String, Set<Long>> entry : subsysTypedAppIds.get(subsysId).entrySet()) {
                subsysAppSummary.addTypedAppSummary(entry.getKey(),
                        ApplicationFormDetail.ApplicationType.getDisplayOrValue(entry.getKey()),
                        entry.getValue().size());
            }
            result.put(subsysId, subsysAppSummary);
        }
        return result;
    }

    /**
     * 获取业务系统的负责人信息
     */
    public Map<Long, List<OptionSystemUser>> getOwners(Long[] subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> subsysUsers;
        if ((subsysUsers = PermissionService.getRoleSubsystemUserIdsNoInherited(
                SystemRoleService.InternalRoles.Admin.getCode(), subsystemIds)) == null || subsysUsers.size() <= 0) {
            return Collections.emptyMap();
        }
        Set<Long> allUsers = new HashSet<>();
        for (Set<Long> v : subsysUsers.values()) {
            allUsers.addAll(v);
        }
        List<OptionSystemUser> allOptionUsers;
        if ((allOptionUsers = ClassUtil.getSingltonInstance(FieldSystemUser.class)
                .queryDynamicValues(allUsers.toArray(new Long[0]))) == null || allOptionUsers.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, OptionSystemUser> mapOptionUsers = new HashMap<>();
        for (OptionSystemUser aou : allOptionUsers) {
            mapOptionUsers.put(aou.getId(), aou);
        }
        OptionSystemUser foundOptionUser;
        List<OptionSystemUser> sysOptionUsers;
        Map<Long, List<OptionSystemUser>> result = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> e : subsysUsers.entrySet()) {
            result.put(e.getKey(), sysOptionUsers = new ArrayList<>());
            for (Long uid : e.getValue()) {
                if ((foundOptionUser = mapOptionUsers.get(uid)) != null) {
                    sysOptionUsers.add(foundOptionUser);
                }
            }
        }
        return result;
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
        if ((productlineAll = ClassUtil.getSingltonInstance(FieldDepartment.class)
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
     * 通过业务系统的编号，获取清单
     */
    private <T extends SubsystemFormSimple> List<T> list(Class<T> clazz, boolean disableIncluded,
            Long... subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyList();
        }
        return list(clazz, new SubsystemQueryAll(subsystemIds.length, 1L).setDisableIncluded(disableIncluded)
                .setIdsIn(StringUtils.join(subsystemIds, ','))).getList();
    }
    
    /**
     * 通过业务系统的代码，获取清单
     */
    public <T extends SubsystemFormSimple> List<T> list(Class<T> clazz, boolean disableIncluded,
            String... subsystemCodes) throws Exception {
        if (subsystemCodes == null || subsystemCodes.length <= 0) {
            return Collections.emptyList();
        }
        return list(clazz, new SubsystemQueryAll(subsystemCodes.length, 1L).setDisableIncluded(disableIncluded)
                .setCodesIn(StringUtils.join(subsystemCodes, ','))).getList();
    }
    
    public <T extends SubsystemFormSimple> PagedList<T> list(@NonNull Class<T> clazz,
            @NonNull SubsystemQueryAccessable query) throws Exception {
        return listForm(clazz, query);
    }
    
    /**
     * 获取业务系统的代码可访问人员清单(包括代码下载、变更和仓库管理)
     */
    public List<? extends AbstractUser> collectAllCodePermGroupMembers(long subsystemId) throws Exception {
        List<Long> members = PermissionService.querySubsystemUsersByAuthKey(subsystemId, true,
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
        if ((subsystem = get(SubsystemFormSimple.class, subsystemId)) == null) {
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
        // TODO Auto-generated method stub
        
    }
}
