package org.socyno.webfwk.state.module.access;

import lombok.Getter;
import lombok.SneakyThrows;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleCleaner;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.module.user.SystemUserFormSimple;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SystemAccessApplyService extends
        AbstractStateFormServiceWithBaseDao<SystemAccessApplyFormDetail, SystemAccessApplyFormDefault, SystemAccessApplyFormSimple> {
    
    private SystemAccessApplyService(){
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemAccessApplyService Instance = new SystemAccessApplyService();
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "待提交"),
        SUBMITTED("submitted", "待审批"),
        LEADER_PASSED("leader_passed", "待业务系统负责人审批"),
        LEADER_REJECTED("leader_rejected", "直属领导审批拒绝"),
        OWNER_PASSED("owner_passed", "待系统管理员审批"),
        OWNER_REJECTED("owner_rejected", "业务系统负责人审批拒绝"),
        ADMIN_REJECTED("admin_rejected", "系统管理员审批拒绝"),
        COMPLETED("completed","审批完成"),
        DELETED("deleted","已撤销");

        private final String code;
        private final String name;

        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum EVENTS implements StateFormEventClassEnum {

        /**
         * 创建申请
         */
        Create(EventCreate.class),
        /**
         * 修改申请
         */
        Update(EventEdit.class),
        /**
         * 提交申请
         */
        Submit(EventSubmit.class),
        /**
         * 撤销申请
         */
        Delete(EventDelete.class),
        /**
         * 直属领导审批（同意）
         */
        LeaderPass(EventLeaderPass.class),
        /**
         * 直属领导审批（拒绝）
         */
        LeaderReject(EventLeaderReject.class),
        /**
         * 业务负责人审批（同意）
         */
        OwnerPass(EventOwnerPass.class),
        /**
         * 业务负责人审批（拒绝）
         */
        OwnerReject(EventOwnerReject.class),
        /**
         * 系统管理员审批（同意）
         */
        AdminPass(EventAdminPass.class),
        /**
         * 系统管理员审批（拒绝）
         */
        AdminReject(EventAdminReject.class),
        
        /**
         * 各类代办事项
         */
        TodoCreateForLeader(EventTodoCreateForLeader.class),
        TodoCloseForLeader(EventTodoCloseForLeader.class),
        TodoCreateForOwner(EventTodoCreateForOwner.class),
        TodoCloseForOwner(EventTodoCloseForOwner.class),
        TodoCreateForAdmin(EventTodoCreateForAdmin.class),
        TodoCloseForAdmin(EventTodoCloseForAdmin.class);
        ;

        private final Class<? extends AbstractStateAction<SystemAccessApplyFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemAccessApplyFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemAccessApplyFormDefault>("默认查询",
                SystemAccessApplyFormDefault.class, SystemAccessApplyQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemAccessApplyFormSimple> forms) throws Exception {
        
        Map<Long, List<SystemAccessApplyFormSimple>> withDetails = new HashMap<>();
        for (SystemAccessApplyFormSimple form : forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            List<SystemAccessApplyFormSimple> sameDetails;
            if (SystemAccessApplyWithDetails.class.isAssignableFrom(form.getClass())) {
                if ((sameDetails = withDetails.get(form.getId())) == null) {
                    withDetails.put(form.getId(), sameDetails = new ArrayList<>());
                }
                sameDetails.add(form);
            }
        }
        
        if (withDetails.size() > 0) {
            List<SystemAccessApplyBusinessEntity> flattedBusinessEntities = queryBusinessEntitiesByAppliyIds(
                    withDetails.keySet().toArray(new Long[0]));
            DefaultStateFormSugger.getInstance().apply(flattedBusinessEntities);
            List<SystemAccessApplyBusinessEntity> requestBusinessEntities;
            Map<Long, List<SystemAccessApplyBusinessEntity>> groupedBusinessEntities = new HashMap<>();
            for (SystemAccessApplyBusinessEntity e : flattedBusinessEntities) {
                long requestId = e.getApplyId();
                if ((requestBusinessEntities = groupedBusinessEntities.get(requestId)) == null) {
                    groupedBusinessEntities.put(requestId, requestBusinessEntities = new ArrayList<>());
                }
                requestBusinessEntities.add(e);
                if (!e.isBusinessEntity()) {
                    e.setBusiness(FieldSystemBusinessAccessApply.genSystemBusinessOption());
                }
            }
            for (Map.Entry<Long, List<SystemAccessApplyFormSimple>> e : withDetails.entrySet()) {
                for (SystemAccessApplyFormSimple form : e.getValue()) {
                    form.setBusinessEntities(groupedBusinessEntities.get(e.getKey()));
                }
            }
        }
    }
    
    /**
     *  SELECT * FROM %s WHERE apply_id IN (%s)
     */
    @Multiline
    private final static String SQL_QUERY_BUSINESSES_BY_APPLYID = "X";
    
    private List<SystemAccessApplyBusinessEntity> queryBusinessEntitiesByAppliyIds(Long... applyIds) throws Exception {
        if (applyIds == null || applyIds.length <= 0) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(SystemAccessApplyBusinessEntity.class,
                String.format(SQL_QUERY_BUSINESSES_BY_APPLYID, getBusinessEntityTable(),
                        CommonUtil.join("?", applyIds.length, ",")),
                applyIds);
    }
    
    @Override
    protected String getFormTable() {
        return "system_access_request";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "system_access_request";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统授权申请单";
    }
    
    public static class OwnerChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((SystemAccessApplyFormSimple) form).getCreatedBy());
        }
        
    }
    
    public class EventCreate
            extends AbstractStateCreateAction<SystemAccessApplyFormDetail, SystemAccessApplyFormCreation> {
        
        public EventCreate() {
            super("申请", STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormDetail form, String message) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemAccessApplyFormDetail originForm,
                SystemAccessApplyFormCreation form, String message) throws Exception {
            return new StateFormEventResultCreateViewBasic(save(event, form));
        }
    }
    
    public class EventEdit
            extends AbstractStateAction<SystemAccessApplyFormDetail, SystemAccessApplyFormEdition, Void> {
        
        public EventEdit() {
            super("编辑",
                    getStateCodes(STATES.CREATED, STATES.OWNER_REJECTED, STATES.LEADER_REJECTED, STATES.ADMIN_REJECTED),
                    STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = OwnerChecker.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, SystemAccessApplyFormEdition form,
                String message) throws Exception {
            save(event, form);
            return null;
        }
    }
    
    public class EventSubmit extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventSubmit() {
            super("提交", getStateCodes(STATES.CREATED), STATES.SUBMITTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = OwnerChecker.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                String sourceState) throws Exception {
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventDelete() {
            super("撤销",
                    getStateCodes(STATES.CREATED, STATES.ADMIN_REJECTED, STATES.LEADER_REJECTED, STATES.OWNER_REJECTED),
                    STATES.DELETED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = OwnerChecker.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                String sourceState) throws Exception {
            
            return null;
        }
    }
    
    public class EventLeaderPass
            extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventLeaderPass() {
            super("审批通过(L)", getStateCodes(STATES.SUBMITTED), new AbstractStateChoice(
                    "是否包含业务授权申请？",
                    STATES.LEADER_PASSED.getCode(),
                    STATES.OWNER_PASSED.getCode()) {
                @SneakyThrows
                @Override
                protected boolean select(AbstractStateFormInput abstractStateForm) {
                    List<SystemAccessApplyBusinessEntity> entities;
                    if ((entities = ((SystemAccessApplyFormDetail) getContextFormOrigin())
                            .getBusinessEntities()) != null) {
                        for (SystemAccessApplyBusinessEntity e : entities) {
                            if (e.isBusinessEntity()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = DirectLeaderChecker.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
        
    }
    
    public class DirectLeaderChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) throws Exception {
            if (form == null) {
                return false;
            }
            SystemUserFormSimple systemUserSimple = SystemUserService.getInstance()
                    .getSimple(((SystemAccessApplyFormSimple) form).getCreatedBy());
            return systemUserSimple != null && systemUserSimple.getManager().equals(SessionContext.getUserId());
        }
        
    }
    
    public class EventLeaderReject
            extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventLeaderReject() {
            super("审批拒绝(L)", getStateCodes(STATES.SUBMITTED), STATES.LEADER_REJECTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = DirectLeaderChecker.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
    }
    
    public class EventOwnerPass
            extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventOwnerPass() {
            super("审批通过(O)", getStateCodes(STATES.LEADER_PASSED),
                    STATES.OWNER_PASSED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, multipleParser = AuthorityBusinessesParser.class, multipleChoice = true)
        public void check(String event, SystemAccessApplyFormDetail originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
        
    }
    
    public class EventOwnerReject
            extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventOwnerReject() {
            super("审批拒绝(O)", getStateCodes(STATES.LEADER_PASSED),
                    STATES.OWNER_REJECTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, multipleParser = AuthorityBusinessesParser.class, multipleCleaner = AuthorityBusinessesParserClear.class)
        public void check(String event, SystemAccessApplyFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
    }
    
    public class EventAdminPass extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventAdminPass() {
            super("审批通过(M)", getStateCodes(STATES.OWNER_PASSED), STATES.COMPLETED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormDetail originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            for (SystemAccessApplyBusinessEntity entity : originForm.getBusinessEntities()) {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        "system_user_scope_role",
                        new ObjectMap().put("=user_id", originForm.getCreatedBy())
                                .put("scope_type", entity.isBusinessEntity() ? "Business" : "System")
                                .put("scope_id", entity.getBusiness().getId())
                                .put("role_id", entity.getRole().getId())));
            }
            
            return null;
        }
    }
    
    public class EventAdminReject extends AbstractStateAction<SystemAccessApplyFormDetail, StateFormBasicInput, Void> {
        
        public EventAdminReject() {
            super("审批拒绝(M)", getStateCodes(STATES.OWNER_PASSED), STATES.ADMIN_REJECTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormDetail originForm, StateFormBasicInput form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
    }
    
    public class EventTodoCreateForLeader extends AbstractStateTodoApprovalAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCreateForLeader() {
            super("创建申请人领导待办事项", STATES.SUBMITTED.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormDetail originForm, AbstractStateFormInput form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "申请人领导审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormDetail originForm, AbstractStateFormInput form)
                throws Exception {
            SystemUserFormSimple systemUserSimple = (SystemUserFormSimple) SystemUserService.getInstance()
                    .getSimple(SessionContext.getUserId());
            if (systemUserSimple == null) {
                return null;
            }
            if (systemUserSimple.getManager() == null) {
                throw new MessageException("当前用户直属领导不存在，无法提交申请单。");
            }
            return new long[] { systemUserSimple.getManager().getId() };
        }
        
        @Override
        protected String getNextRejectEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.LeaderReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.LeaderPass.getName();
        }
    }
    
    public class EventTodoCloseForLeader extends AbstractStateTodoCloseAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCloseForLeader() {
            super("关闭申请人领导待办事项", STATES.SUBMITTED.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) throws Exception {
            return EVENTS.TodoCreateForLeader.getName();
        }
    }
    
    public class EventTodoCreateForOwner extends AbstractStateTodoApprovalAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCreateForOwner() {
            super("创建业务系统负责人的待办事项", STATES.LEADER_PASSED.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormDetail originForm, AbstractStateFormInput form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "业务负责人审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) throws Exception {
            
            List<String> businessIds = new ArrayList<>();
            if (originForm.getBusinessEntities() != null) {
                for (SystemAccessApplyBusinessEntity e : originForm.getBusinessEntities()) {
                    if (e.isBusinessEntity()) {
                        businessIds.add(e.getBusiness().getId());
                    }
                }
            }
            Map<String, List<OptionSystemUser>> subsysOwners;
            if (businessIds.isEmpty()
                    || (subsysOwners = PermissionService.getBusinessOwners(businessIds.toArray(new String[0]))) == null
                    || subsysOwners.isEmpty()) {
                throw new MessageException("申请的业务系统未指定负责人，请联系系统管理员");
            }
            List<OptionSystemUser> oneOwners;
            Set<Long> assignee = new HashSet<>();
            for (String sysid : businessIds) {
                if ((oneOwners = subsysOwners.get(sysid)) == null) {
                    throw new MessageException(String.format("申请的业务系统(%s)未指定负责人，请联系系统管理员", sysid));
                }
                for (OptionSystemUser o : oneOwners) {
                    assignee.add(o.getId());
                }
            }
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(assignee.toArray(new Long[0]));
        }
        
        @Override
        protected String getNextRejectEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.OwnerReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.OwnerPass.getName();
        }
    }
    
    public class EventTodoCloseForOwner extends AbstractStateTodoCloseAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCloseForOwner() {
            super("关闭业务系统负责人的待办事项", STATES.LEADER_PASSED.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) throws Exception {
            return EVENTS.TodoCreateForOwner.getName();
        }
    }
    
    public class EventTodoCreateForAdmin extends AbstractStateTodoApprovalAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCreateForAdmin() {
            super("创建SCM待办事项", STATES.OWNER_PASSED.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormDetail originForm, AbstractStateFormInput form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "SCM审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormDetail originForm, AbstractStateFormInput form)
                throws Exception {
            
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(
                    getActionUserIds(EVENTS.AdminPass.getName(), getForm(form.getId())));
        }
        
        @Override
        protected String getNextRejectEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.AdminReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) {
            return EVENTS.AdminPass.getName();
        }
    }
    
    public class EventTodoCloseForAdmin extends AbstractStateTodoCloseAction<SystemAccessApplyFormDetail> {
        
        public EventTodoCloseForAdmin() {
            super("关闭SCM待办事项", STATES.OWNER_PASSED.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormDetail originForm,
                AbstractStateFormInput form) throws Exception {
            return EVENTS.TodoCreateForAdmin.getName();
        }
    }
    
    private String getBusinessEntityTable() {
        return "system_access_request_bussiness";
    }
    
    private void saveBusinessApplied(long formId, List<SystemAccessApplyBusinessEntity> entities) throws Exception {
        getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareDeleteQuery(getBusinessEntityTable(), new ObjectMap().put("=apply_id", formId)));
        if (entities == null) {
            return;
        }
        for (SystemAccessApplyBusinessEntity entity : entities) {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getBusinessEntityTable(), new ObjectMap()
                            .put("access_type" , entity.getAccessType())
                            .put("business", entity.getBusiness().getId())
                            .put("role" , entity.getRole().getId())
                            .put("=apply_id" , formId)
            ));
        }
    }
    
    public class AuthorityBusinessesParser implements AuthorityScopeIdMultipleParser {
        @SneakyThrows
        @Override
        public String[] getAuthorityScopeIds(Object form) {
            Set<String> bussinessIds = new HashSet<>();
            List<SystemAccessApplyBusinessEntity> applied;
            if ((applied = ((SystemAccessApplyFormDetail) form).getBusinessEntities()) != null) {
                for (SystemAccessApplyBusinessEntity entity : applied) {
                    if (entity != null && entity.isBusinessEntity()) {
                        if (StringUtils.isNotBlank(entity.getBusiness().getId())) {
                            bussinessIds.add(entity.getBusiness().getId());
                        }
                    }
                }
            }
            return bussinessIds.toArray(new String[0]);
        }
    }
    
    public class AuthorityBusinessesParserClear implements AuthorityScopeIdMultipleCleaner {
        @Override
        public String[] getEventsToClean() throws Exception {
            return new String[0];
        }
    }
    
    private long save(String evnet, SystemAccessApplyFormCreation form) throws Exception {

        List<SystemAccessApplyBusinessEntity> businesses;
        if ((businesses = form.getBusinesses()) == null || businesses.isEmpty()) {
            throw new MessageException("至少需要申请一项业务及角色权限");
        }
        for (SystemAccessApplyBusinessEntity entity : businesses) {
            if (entity.isBusinessEntity()
                    && (entity.getRole() == null || entity.getBusiness() == null)) {
                throw new MessageException("申请的业务权限信息填写不完整");
            }
        }
        
        Set<Long> roleIds = new HashSet<>();
        for (SystemAccessApplyBusinessEntity entity : businesses) {
            roleIds.add(entity.getRole().getId());
        }
        List<OptionSystemRole> roleEntities = ClassUtil.getSingltonInstance(FieldSystemRole.class)
                .queryDynamicValues(roleIds.toArray());
        StringBuilder title = new StringBuilder();
        for (OptionSystemRole role : roleEntities) {
            StringUtils.appendIfNotEmpty(title, ",").append(role.getName());
        }
        title.insert(0, getFormDisplay().concat(": "));
        
        AtomicLong id = new AtomicLong(0);
        if (isCreateAction(evnet)) {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("description", form.getDescription())
                            .put("title", StringUtils.truncate(title.toString(), 128))
            ), new AbstractDao.ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    resultSet.next();
                    id.set(resultSet.getLong(1));
                }
            });
        } else {
            id.set(form.getId());
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("description", form.getDescription())
                            .put("title", StringUtils.truncate(title.toString(), 128))
            ));
        }
        saveBusinessApplied(id.get(), businesses);
        return id.get();
    }
}
