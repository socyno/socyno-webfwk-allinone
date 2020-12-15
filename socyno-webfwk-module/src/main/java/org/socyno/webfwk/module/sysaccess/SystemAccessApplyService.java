package org.socyno.webfwk.module.sysaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleCleaner;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserService;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        APPLICANT_LEADER_PASS("applicant_leader_pass", "待业务系统负责人审批"),
        APPLICANT_LEADER_REJECT("applicant_leader_reject", "申请人领导审批拒绝"),
        SUBSYSTEM_OWNER_APPROVAL_PASS("subsystem_owner_approval_pass", "待配置管理组审批"),
        SUBSYSTEM_OWNER_APPROVAL_REJECT("subsystem_owner_approval_reject", "业务系统负责人审批拒绝"),
        SCM_PASS("scm_pass", "配置管理组审批通过"),
        SCM_REJECT("scm_reject", "配置管理组审批拒绝"),
        APPROVAL_COMPLETED("approval_completed","审批完成"),
        ABOLITION("abolition","废除");

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
         * 创建申请单
         */
        Create(EventCreate.class),
        /**
         * 修改申清单
         */
        Update(EventEdit.class),
        /**
         * 提交申清单
         */
        Submit(EventSubmit.class),
        /**
         * 废除
         */
        Abolition(EventAbolition.class),

        ApplicantLeaderPassed(EventApplicantLeaderPassed.class),
        ApplicantLeaderRejected(EventApplicantLeaderReject.class),
        SubsystemOwnerApprovePassed(EventSubsystemOwnerApprovePassed.class),
        SubsystemOwnerApproveRejected(EventSubsystemOwnerApproveRejected.class),
        ScmPassed(EventScmPassed.class),
        ScmReject(EventScmReject.class),

        ApplicantLeaderCreate(EventApplicantLeaderCreate.class),
        ApplicantLeaderClose(EventApplicantLeaderClose.class),
        SubsystemOwnerApproveCreate(EventSubsystemOwnerApproveCreate.class),
        SubsystemOwnerApproveClose(EventSubsystemOwnerApproveClose.class),
        ScmCreate(EventScmCreate.class),
        ScmClose(EventScmClose.class);

        ;

        private final Class<? extends AbstractStateAction<SystemAccessApplyFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemAccessApplyFormSimple, ?, ?>> eventClass) {
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
        
        Map<Long, SystemAccessApplyWithSubSystems> withSubSystems = new HashMap<>();
        for (SystemAccessApplyFormSimple form : forms) {
            if (form == null) {
                continue;
            }
            if (SystemAccessApplyWithSubSystems.class.isAssignableFrom(form.getClass())) {
                withSubSystems.put(form.getId(), form);
            }
        }
        
        if (withSubSystems.size() > 0) {
            List<SystemAccessApplySubSystemEntity> ports = querySubSystemsByAccessRequestIds(
                    withSubSystems.keySet().toArray(new Long[0]));
            Map<Long, List<SystemAccessApplySubSystemEntity>> subSystems = ports.stream()
                    .collect(Collectors.groupingBy(SystemAccessApplySubSystemEntity::getAccessRequestId));
            for (SystemAccessApplyFormSimple form : forms) {
                if (form.getSubSystems() == null) {
                    List<SystemAccessApplySubSystemEntity> entityList = subSystems.get(form.getId());
                    for (SystemAccessApplySubSystemEntity accessRequestSubSystemEntity : entityList) {
                        accessRequestSubSystemEntity
                                .setSubsystem(ClassUtil.getSingltonInstance(FieldSubsystemAccessApply.class)
                                        .queryDynamicValue(accessRequestSubSystemEntity.getSubsystemId()));
                        accessRequestSubSystemEntity.setRole(ClassUtil.getSingltonInstance(FieldSystemRole.class)
                                .queryDynamicValues(new Long[] { accessRequestSubSystemEntity.getRoleId() }).get(0));
                    }
                    form.setSubSystems(entityList);
                }
            }
        }
    }
    
    /**
     *  select * from access_request_subsystem where access_request_id in (%s)
     */
    @Multiline
    private final static String SQL_QUERY_ACCESSREQUESTS_BY_ACCESS_REQUEST_ID = "X";
    
    public List<SystemAccessApplySubSystemEntity> querySubSystemsByAccessRequestIds(Long... Ids) throws Exception {
        if (Ids == null || Ids.length <= 0) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(SystemAccessApplySubSystemEntity.class,
                String.format(SQL_QUERY_ACCESSREQUESTS_BY_ACCESS_REQUEST_ID, CommonUtil.join("?", Ids.length, ",")),
                Ids);
    }
    
    @Override
    protected String getFormTable() {
        return "access_request";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "access_request";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统权限申请单";
    }
    
    public static class userChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null && SessionContext.getUserId().equals(((SystemAccessApplyFormSimple) form).getCreatedBy());
        }
        
    }
    
    public class EventCreate extends AbstractStateSubmitAction<SystemAccessApplyFormSimple, SystemAccessApplyFormCreation> {
        
        public EventCreate() {
            super("申请", STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormSimple form, String message) {
            
        }
        
        @Override
        public Long handle(String event, SystemAccessApplyFormSimple originForm, SystemAccessApplyFormCreation form, String message)
                throws Exception {
            
            if (form.getSubSystems().size() == 0) {
                throw new MessageException("请至少填写一项申请信息!");
            } else {
                for (SystemAccessApplySubSystemEntity subSystem : form.getSubSystems()) {
                    if (subSystem.getAccessType().equals("subSystem")) {
                        if (subSystem.getRole() == null) {
                            throw new MessageException("业务系统请填写角色信息!");
                        }
                        if (subSystem.getSubsystem() == null) {
                            throw new MessageException("业务系统请填写业务系统信息!");
                        }
                    }
                }
                
                String title = "";
                List<Long> subSystems = new ArrayList<>();
                Set<Long> roleList = new HashSet<>();
                for (SystemAccessApplySubSystemEntity subSystem : form.getSubSystems()) {
                    if (subSystem.getAccessType().equals("subSystem")) {
                        subSystems.add(subSystem.getSubsystem().getId());
                    }
                    roleList.add(subSystem.getRole().getId());
                }
                List<OptionSystemRole> roleNames = ClassUtil.getSingltonInstance(FieldSystemRole.class)
                        .queryDynamicValues(roleList.toArray());
                for (OptionSystemRole roleName : roleNames) {
                    title += roleName.getName() + ",";
                }
                title = title.substring(0, title.length() - 1);
                
                AtomicLong id = new AtomicLong(0);
                getFormBaseDao().executeUpdate(SqlQueryUtil
                        .prepareInsertQuery(
                        getFormTable(), new ObjectMap()
                                .put("created_by", SessionContext.getUserId())
                                .put("created_at", new Date())
                                .put("created_code_by", SessionContext.getUsername())
                                .put("created_name_by", SessionContext.getDisplay())
                                .put("reason_for_application", form.getReasonForApplication())
                                .put("subsystem_count", subSystems.size())
                                .put("title", title.length()>50?title.substring(0,50)+"...":title)
                ), new AbstractDao.ResultSetProcessor() {
                    @Override
                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                        resultSet.next();
                        id.set(resultSet.getLong(1));
                    }
                });
                saveSubSystems(id.get(), form.getSubSystems());
                return id.get();
            }
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemAccessApplyFormSimple, SystemAccessApplyFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodes(STATES.CREATED, STATES.SUBSYSTEM_OWNER_APPROVAL_REJECT,
                    STATES.APPLICANT_LEADER_REJECT, STATES.SCM_REJECT), STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = userChecker.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, SystemAccessApplyFormEdition form, String message)
                throws Exception {
            
            if (form.getSubSystems().size() == 0) {
                throw new MessageException("请至少填写一项申请信息!");
            } else {
                for (SystemAccessApplySubSystemEntity subSystem : form.getSubSystems()) {
                    if (subSystem.getAccessType().equals("subSystem")) {
                        if (subSystem.getRole() == null) {
                            throw new MessageException("业务系统请填写角色信息!");
                        }
                        if (subSystem.getSubsystem() == null) {
                            throw new MessageException("业务系统请填写业务系统信息!");
                        }
                    }
                }
                
                String title = "";
                List<Long> subSystems = new ArrayList<>();
                Set<Long> roleList = new HashSet<>();
                for (SystemAccessApplySubSystemEntity subSystem : form.getSubSystems()) {
                    if (subSystem.getAccessType().equals("subSystem")) {
                        subSystems.add(subSystem.getSubsystem().getId());
                    }
                    roleList.add(subSystem.getRole().getId());
                    
                }
                
                List<OptionSystemRole> roleNames = ClassUtil.getSingltonInstance(FieldSystemRole.class)
                        .queryDynamicValues(roleList.toArray());
                for (OptionSystemRole roleName : roleNames) {
                    title += roleName.getName() + ",";
                }
                title = title.substring(0, title.length() - 1);
                
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                        new ObjectMap().put("=id", form.getId())
                                .put("reason_for_application", form.getReasonForApplication())
                                .put("subsystem_count", subSystems.size())
                                .put("title", title.length() > 50 ? title.substring(0, 50) + "..." : title)));
                saveSubSystems(form.getId(), form.getSubSystems());
            }
            return null;
        }

    }

    public class EventSubmit extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void>{

        public EventSubmit(){
            super("提交" , getStateCodes(STATES.CREATED) , STATES.SUBMITTED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System,checker = userChecker.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event , SystemAccessApplyFormSimple originForm , BasicStateForm form , String sourceState) throws Exception{



            return null ;
        }
    }

    public class EventAbolition extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void>{

        public EventAbolition(){
            super("废除" , getStateCodes(STATES.CREATED,STATES.SCM_REJECT,STATES.APPLICANT_LEADER_REJECT,STATES.SUBSYSTEM_OWNER_APPROVAL_REJECT) , STATES.ABOLITION.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System,checker = userChecker.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event , SystemAccessApplyFormSimple originForm , BasicStateForm form , String sourceState) throws Exception{

            return null ;
        }
    }

    public class EventApplicantLeaderPassed
            extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventApplicantLeaderPassed() {
            super("审批通过(L)", getStateCodes(STATES.SUBMITTED), new AbstractStateChoice("是否包含业务系统？",
                    STATES.APPLICANT_LEADER_PASS.getCode(), STATES.SUBSYSTEM_OWNER_APPROVAL_PASS.getCode()) {
                @SneakyThrows
                @Override
                protected boolean select(AbstractStateForm abstractStateForm) {
                    Integer subsystemCount = getFormBaseDao().queryAsObject(Integer.class,
                            "select subsystem_count from access_request where id = ?",
                            new Object[] { abstractStateForm.getId() });
                    if (subsystemCount > 0) {
                        return true;
                    }
                    return false;
                }
            });
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = DirectLeaderChecker.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
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
    
    public class EventApplicantLeaderReject
            extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventApplicantLeaderReject() {
            super("审批拒绝(L)", getStateCodes(STATES.SUBMITTED), STATES.APPLICANT_LEADER_REJECT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = DirectLeaderChecker.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
    }
    
    public class EventSubsystemOwnerApprovePassed
            extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventSubsystemOwnerApprovePassed() {
            super("审批通过(O)", getStateCodes(STATES.APPLICANT_LEADER_PASS),
                    STATES.SUBSYSTEM_OWNER_APPROVAL_PASS.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, multipleParser = AppSubsystemParser.class, multipleChoice = true)
        public void check(String event, SystemAccessApplyFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
        
    }
    
    public class EventSubsystemOwnerApproveRejected
            extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventSubsystemOwnerApproveRejected() {
            super("审批拒绝(O)", getStateCodes(STATES.APPLICANT_LEADER_PASS),
                    STATES.SUBSYSTEM_OWNER_APPROVAL_REJECT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, multipleParser = AppSubsystemParser.class, multipleCleaner = AppSubsystemParserClear.class)
        public void check(String event, SystemAccessApplyFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
    }
    
    public class EventScmPassed extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventScmPassed() {
            super("审批通过(M)", getStateCodes(STATES.SUBSYSTEM_OWNER_APPROVAL_PASS), STATES.APPROVAL_COMPLETED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
                final String message) throws Exception {
            for (SystemAccessApplySubSystemEntity entity : originForm.getSubSystems()) {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_user_scope_role",
                        new ObjectMap().put("=user_id", originForm.getCreatedBy())
                                .put("scope_type", entity.getAccessType().equals("subSystem") ? "Subsystem" : "System")
                                .put("scope_id",
                                        entity.getAccessType().equals("subSystem") ? entity.getSubsystem().getId() : 0)
                                .put("role_id", entity.getRoleId())));
            }
            
            return null;
        }
    }
    
    public class EventScmReject extends AbstractStateAction<SystemAccessApplyFormSimple, BasicStateForm, Void> {
        
        public EventScmReject() {
            super("审批拒绝(M)", getStateCodes(STATES.SUBSYSTEM_OWNER_APPROVAL_PASS), STATES.SCM_REJECT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemAccessApplyFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemAccessApplyFormSimple originForm, BasicStateForm form,
                final String message) throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
    }
    
    public class EventApplicantLeaderCreate extends AbstractStateTodoApprovalAction<SystemAccessApplyFormSimple> {
        
        public EventApplicantLeaderCreate() {
            super("创建申请人领导待办事项", STATES.SUBMITTED.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "申请人领导审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
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
        protected String getNextRejectEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.ApplicantLeaderRejected.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.ApplicantLeaderPassed.getName();
        }
    }
    
    public class EventApplicantLeaderClose extends AbstractStateTodoCloseAction<SystemAccessApplyFormSimple> {
        
        public EventApplicantLeaderClose() {
            super("关闭申请人领导待办事项", STATES.SUBMITTED.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) throws Exception {
            return EVENTS.ApplicantLeaderCreate.getName();
        }
    }
    
    public class EventSubsystemOwnerApproveCreate extends AbstractStateTodoApprovalAction<SystemAccessApplyFormSimple> {
        
        public EventSubsystemOwnerApproveCreate() {
            super("创建业务系统负责人的待办事项", STATES.APPLICANT_LEADER_PASS.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "业务负责人审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
                throws Exception {
            
            List<Long> subsystemIds = new ArrayList<>();
            if (originForm.getSubSystems() != null) {
                for (SystemAccessApplySubSystemEntity nrccEntity : originForm.getSubSystems()) {
                    if (nrccEntity.getAccessType().equals("subSystem")) {
                        subsystemIds.add(nrccEntity.getSubsystem().getId());
                    }
                }
            }
            
            Set<Long> assignee = new HashSet<>();
            for (Long subsystemId : subsystemIds) {
                List<OptionSystemUser> subsysOwners;
                if ((subsysOwners = SubsystemService.getInstance().getOwners(subsystemId)) == null
                        || subsysOwners.isEmpty()) {
                    throw new MessageException("业务系统不存在或未定义负责人，无法提交申请单。");
                }
                Long[] todoAssignee = new Long[subsysOwners.size()];
                for (int i = 0; i < todoAssignee.length; i++) {
                    todoAssignee[i] = subsysOwners.get(i).getId();
                }
                Collections.addAll(assignee, todoAssignee);
            }
            Long[] todoAssigneeAll = new Long[assignee.size()];
            for (int i = 0; i < assignee.size(); i++) {
                todoAssigneeAll[i] = Long.parseLong(assignee.toArray()[i] + "");
            }
            
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(todoAssigneeAll);
        }
        
        @Override
        protected String getNextRejectEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.SubsystemOwnerApproveRejected.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.SubsystemOwnerApprovePassed.getName();
        }
    }
    
    public class EventSubsystemOwnerApproveClose extends AbstractStateTodoCloseAction<SystemAccessApplyFormSimple> {
        
        public EventSubsystemOwnerApproveClose() {
            super("关闭业务系统负责人的待办事项", STATES.APPLICANT_LEADER_PASS.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) throws Exception {
            return EVENTS.SubsystemOwnerApproveCreate.getName();
        }
    }
    
    public class EventScmCreate extends AbstractStateTodoApprovalAction<SystemAccessApplyFormSimple> {
        
        public EventScmCreate() {
            super("创建SCM待办事项", STATES.SUBSYSTEM_OWNER_APPROVAL_PASS.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return String.format("%s:%s - %s", originForm.getTitle(), getFormDisplay(), "SCM审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SystemAccessApplyFormSimple originForm, AbstractStateForm form)
                throws Exception {
            
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(
                    getActionUserIds(EVENTS.ScmPassed.getName(), getForm(form.getId())));
        }
        
        @Override
        protected String getNextRejectEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.ScmReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) {
            return EVENTS.ScmPassed.getName();
        }
    }
    
    public class EventScmClose extends AbstractStateTodoCloseAction<SystemAccessApplyFormSimple> {
        
        public EventScmClose() {
            super("关闭SCM待办事项", STATES.SUBSYSTEM_OWNER_APPROVAL_PASS.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SystemAccessApplyFormSimple originForm,
                AbstractStateForm form) throws Exception {
            return EVENTS.ScmCreate.getName();
        }
    }
    
    private void saveSubSystems(long formId, List<SystemAccessApplySubSystemEntity> entities) throws Exception {
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("access_request_subsystem",
                new ObjectMap().put("=access_request_id", formId)));
        if (entities == null) {
            return;
        }
        for (SystemAccessApplySubSystemEntity entity : entities) {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "access_request_subsystem" , new ObjectMap()
                            .put("access_request_id" , formId)
                            .put("access_type" , entity.getAccessType())
                            .put("subsystem_id" , entity.getAccessType().equals("subSystem")?entity.getSubsystem():0)
                            .put("role_id" , entity.getRole())
            ));
        }
    }
    
    public class AppSubsystemParser implements AuthorityScopeIdMultipleParser {
        
        @SneakyThrows
        @Override
        public long[] getAuthorityScopeIds(Object form) {
            ObjectMapper objectMapper = new ObjectMapper();
            SystemAccessApplyFormDetail simple = objectMapper.convertValue(form, SystemAccessApplyFormDetail.class);
            List<Long> subSystemList = new ArrayList<>();
            if (simple.getSubSystems() != null) {
                for (SystemAccessApplySubSystemEntity subSystem : simple.getSubSystems()) {
                    if (subSystem.getAccessType().equals("subSystem")) {
                        if (subSystem.getSubsystem().getId() != null) {
                            subSystemList.add(subSystem.getSubsystem().getId());
                        }
                    }
                }
            }
            long[] subSystems = subSystemList.stream().mapToLong(t -> t.longValue()).toArray();
            return subSystems;
        }
    }
    
    public class AppSubsystemParserClear implements AuthorityScopeIdMultipleCleaner {
        @Override
        public String[] getEventsToClean() throws Exception {
            return new String[0];
        }
    }
}
