package org.socyno.webfwk.module.sysissue;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.state.model.CommonAttachementItem;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserFormSimple;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.service.AttachmentService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.sugger.SuggerDefinitionFormAttachment;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.*;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class SysIssueService
        extends AbstractStateFormServiceWithBaseDao<SysIssueFormDetail, SysIssueFormDefault, SysIssueFormSimple> {
    
    public static class IsAssigneeChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object originForm) {
            if (originForm == null || SessionContext.getUserId() != null) {
                return false;
            }
            OptionSystemUser assignee;
            if ((assignee = ((SysIssueFormSimple) originForm).getAssignee()) != null
                    && assignee.getId() != null 
                    && assignee.getId().equals(SessionContext.getUserId())) {
                return true;
            }
            return false;
        }
    }
    
    public class IsOwnerChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object originForm) {
            if (originForm == null || SessionContext.getUserId() != null) {
                return false;
            }
            OptionSystemUser createdBy;
            if ((createdBy = ((SysIssueFormSimple) originForm).getCreatedBy()) != null
                    && createdBy.getId() != null 
                    && createdBy.getId().equals(SessionContext.getUserId())) {
                return true;
            }
            return false;
        }
    }
    
    /**
     * 在初始化对象时，事件(EVENTS)的初始化过程中会使用到状态（STATES）列表，
     * 因此务必确保先设置状态再注册事件。
     */
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        SUBMITTED ("submitted", "新提交"),
        ACCEPTED  ("accepted",  "已接收"),
        PAUSED    ("paused",    "挂起"),
        ASSIGNED  ("assigned",  "已分配"),
        CLOSED    ("closed",    "已关闭"),
        REJECTED  ("rejected",  "拒绝");
        
        private final String code;
        private final String name;

        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        
        Submit(EventSubmit.class),
        
        ReSubmit(EventReSubmit.class),
        
        Accept(EventAccept.class),
        
        Reject(EventReject.class),
        
        Assign(EventAssign.class),
        
        Pause(EventPause.class),
        
        Comment(EventComment.class),
        
        Close(EventClose.class),
        
        AssigneeTodoCreate(EventAssigneeTodoCreate.class),
        
        AssigneeTodoClose(EventAssigneeTodoClose.class);

        private final Class<? extends AbstractStateAction<SysIssueFormSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SysIssueFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventSubmit extends AbstractStateSubmitAction<SysIssueFormSimple, SysIssueFormCreation> {
        public EventSubmit() {
            super("提交", STATES.SUBMITTED.getCode());
        }
        
        @Override
        public Long handle(String event, SysIssueFormSimple originForm, final SysIssueFormCreation form, String message) throws Exception {
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("title", form.getTitle())
                            .put("category", categoriesToString(form.getCategory()))
                            .put("description", form.getDescription())
                            .put("submitter", SessionContext.getUserId())
            ), new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            handleAttachments(form.getAttachments(), id.get());
            return id.get();
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
        }
    }
    
    private void handleAttachments(CommonAttachementItem[] attachments, Long id) throws Exception {
        if (attachments != null) {
            Set<Long> attachementIds = new HashSet<>();
            for (CommonAttachementItem item : attachments) {
                if (item == null) {
                    continue;
                }
                attachementIds.add(item.getId());
            }
            AttachmentService.cleanByTargetFormField(getFormName(), id, "attachments");
            AttachmentService.bindWithForm(getFormName(), id, attachementIds.toArray(new Long[0]));
        }
    }
    
    public class EventReSubmit extends AbstractStateAction<SysIssueFormSimple, SysIssueFormEdit, Void> {
        
        public EventReSubmit() {
            super("重新提交", getStateCodes(STATES.REJECTED), STATES.SUBMITTED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return false;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = IsOwnerChecker.class)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SysIssueFormSimple originForm, final SysIssueFormEdit form, final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("title", form.getTitle())
                            .put("category", categoriesToString(form.getCategory()))
                            .put("description", form.getDescription())
            ));
            handleAttachments(form.getAttachments(), form.getId());
            return null;
        }
    }
    
    @SuppressWarnings("unused")
    private String categoriesToString(Collection<OptionDynamicStandard> categories) {
        if (categories == null) {
            return null;
        }
        return categoriesToString(categories.toArray(new OptionDynamicStandard[0]));
    }
    
    private String categoriesToString(OptionDynamicStandard... categories) {
        if (categories == null) {
            return null;
        }
        String[] values = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            values[i] = categories[i].getOptionValue();
        }
        return StringUtils.join(ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[])values), ',');
    }
    
    public class EventAccept extends AbstractStateAction<SysIssueFormSimple, BasicStateForm, Void> {
        public EventAccept() {
            super("接收", STATES.SUBMITTED.getCode(), STATES.ACCEPTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
            
        }
    }
    
    public class EventReject extends AbstractStateAction<SysIssueFormSimple, BasicStateForm, Void> {
        public EventReject() {
            super("拒绝", getStateCodes(STATES.SUBMITTED, STATES.ACCEPTED), STATES.REJECTED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
    }
    
    public class EventAssign extends AbstractStateAction<SysIssueFormSimple, SysIssueFormAssign, Void> {
        public EventAssign() {
            super("分配", getStateCodes(STATES.SUBMITTED, STATES.ASSIGNED, 
                        STATES.ACCEPTED, STATES.PAUSED, STATES.CLOSED),
                    STATES.ASSIGNED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = IsAssigneeChecker.class)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, SysIssueFormSimple originForm, SysIssueFormAssign form, String message)
                throws Exception {
            SystemUserFormSimple sysUser;
            if ((sysUser = SystemUserService.getInstance().getSimple(form.getAssignee().getId())) == null
                    || sysUser.isDisabled()) {
                throw new MessageException("分配的人员不存在或被禁用，请重新分配处理人员。");
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                    .put("=id", form.getId())
                    .put("assignee", sysUser.getId())
                    .put("assign_date", form.getAssignDate())));
            return null;
        }
    }
    
    public class EventPause extends AbstractStateAction<SysIssueFormSimple, BasicStateForm, Void> {
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = IsAssigneeChecker.class)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        public EventPause() {
            super("挂起", getStateCodesEx(STATES.SUBMITTED, STATES.REJECTED, STATES.PAUSED, STATES.CLOSED),
                    STATES.PAUSED.getCode());
        }
    }
    
    public class EventComment extends AbstractStateCommentAction<SysIssueFormSimple> {
        public EventComment() {
            super("添加注释", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
            
        }
    }
    
    public class EventClose extends AbstractStateAction<SysIssueFormSimple, SysIssueFormClose, Void> {
        public EventClose() {
            super("关闭", getStateCodesEx(STATES.SUBMITTED, STATES.REJECTED, STATES.CLOSED), STATES.CLOSED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = IsAssigneeChecker.class)
        public void check(String event, SysIssueFormSimple originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, SysIssueFormSimple originForm, SysIssueFormClose form, String message)
                throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                    .put("=id", form.getId()).put("result", form.getResult()).put("resolution", form.getResolution())));
            return null;
        }
    }
    
    public class EventAssigneeTodoCreate extends AbstractStateTodoCreateAction<SysIssueFormSimple> {
        
        public EventAssigneeTodoCreate() {
            super("创建分配人的待办事项", STATES.ASSIGNED.getCode());
        }
        
        /**
         * 人员分配允许在状态不变的情况下，进行再分配，
         *  
         * 因此必须允许没有状态变更时执行。
         */
        @Override
        protected boolean executeWhenNoStateChanged() {
            return true;
        }
        
        @Override
        protected String getTodoTitle(String event, SysIssueFormSimple originForm, AbstractStateForm form) {
            return String.format("系统报章待处理任务:%s - %s", originForm.getId(), originForm.getTitle());
        }
        
        @Override
        protected long[] getTodoAssignees(String event, SysIssueFormSimple originForm, AbstractStateForm form) {
            return new long[] {((SysIssueFormAssign)form).getAssignee().getId()};
        }
    }
    
    public class EventAssigneeTodoClose extends AbstractStateTodoCloseAction<SysIssueFormSimple> {
        
        public EventAssigneeTodoClose() {
            super("关闭分配人的待办事项", STATES.ASSIGNED.getCode());
        }
        
        /**
         * 人员分配允许在状态不变的情况下，进行再分配，
         *  
         * 因此必须允许没有状态变更时执行。
         */
        @Override
        protected boolean executeWhenNoStateChanged() {
            return true;
        }
        
        @Override
        protected String getClosedTodoEvent(String event, SysIssueFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return EVENTS.AssigneeTodoCreate.getName();
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SysIssueFormDefault>("默认查询",
                SysIssueFormDefault.class, SysIssueQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    private static final SysIssueService Instance = new SysIssueService();
    
    static {
        SuggerDefinitionFormAttachment.addFormName(SysIssueFormDetail.class, Instance.getFormName());
    }
    
    private SysIssueService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_internal_issue";
    }
    
    @Override
    public String getFormTable() {
        return "system_internal_issue";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统报障";
    }

    @Override
    public String getFormIdField() {
        return super.getFormIdField();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SysIssueFormSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
    }
    
    @Override
    protected Map<String, String> getExtraFieldMapper(Class<?> resultClass, Map<String, String> fieldMapper) {
        @SuppressWarnings("serial")
        Map<String, String> finalMapper = new HashMap<String, String>() {{
            put("[,]category", "categories");
            put("submitter", "createdBy");
        }};
        if (fieldMapper != null) {
            finalMapper.putAll(fieldMapper);
        }
        return finalMapper;
    }
}