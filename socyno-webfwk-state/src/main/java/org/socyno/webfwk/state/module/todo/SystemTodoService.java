package org.socyno.webfwk.state.module.todo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import org.socyno.webfwk.state.abs.AbstractStateEnterAction;
import org.socyno.webfwk.state.abs.AbstractStateFormInput;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.abs.AbstractStateLeaveAction;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityEveryoneChecker;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserFormWithSecurity;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tmpl.EnjoyUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

public class SystemTodoService extends
        AbstractStateFormServiceWithBaseDao<SystemTodoFormDetail, SystemTodoFormDefault, SystemTodoFormSimple> {
    
    private SystemTodoService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
        
    }
    
    @Getter
    private static final SystemTodoService Instance = new SystemTodoService();
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum  {
        OPENED("0", "待处理"),
        CLOSED("1", "已关闭")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemTodoFormDefault>("默认查询", 
                SystemTodoFormDefault.class, SystemTodoQueryDefault.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemTodoFormDetail, SystemTodoFormCreation> {
        
        public EventCreate () {
            super("添加", STATES.OPENED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = AuthorityEveryoneChecker.class)
        public void check(String event, SystemTodoFormDetail form, String sourceState) {
            
        }
        
        /**
         * 允许创建事件的并发执行
         */
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemTodoFormDetail originForm, SystemTodoFormCreation form, String message) throws Exception {
            final AtomicLong todoId = new AtomicLong();
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    String targetPage;
                    if (StringUtils.isBlank(targetPage = form.getTargetPage())) {
                        targetPage = EnjoyUtil.format(ContextUtil.getConfigTrimed("system.todo.target.page.tmpl"),
                                (Object) new ObjectMap().put("targetKey", form.getTargetKey())
                                        .put("targetId", form.getTargetId()).asMap());
                    }
                    Long applyUserId = SessionContext.getTokenUserId();
                    String applyUsername = SessionContext.getTokenUsername();
                    String applyDisplay = SessionContext.getTokenDisplay();
                    if (form.getApplyUser().getId() != null) {
                        AbstractUser applier;
                        if ((applier = SystemUserService.getInstance().getSimple(form.getApplyUser().getId())) != null) {
                            applyUserId = applier.getId();
                            applyUsername = applier.getUsername();
                            applyDisplay = applier.getDisplay();
                        }
                    }
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        getFormTable(), new ObjectMap()
                                .put("title", StringUtils.truncate(form.getTitle(), 128))
                                .put("category", form.getCategory())
                                .put("target_key", form.getTargetKey())
                                .put("target_id", form.getTargetId())
                                .put("target_page", targetPage)
                                .put("apply_user_id", applyUserId)
                                .put("apply_user_name", applyUsername)
                                .put("apply_user_display", applyDisplay)
                    ), new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            todoId.set(r.getLong(1));
                        }
                    });
                    setAssignees(todoId.get(), form.getAssignees());
                }
            });
            return new StateFormEventResultCreateViewBasic(todoId.get());
        }
    }
    
    public class EventaWhenCreated extends AbstractStateEnterAction<SystemTodoFormDetail> {
        
        public EventaWhenCreated () {
            super("待办事项创建通知", STATES.OPENED.getCode());
        }
        
        @Override
        public Void handle(String event, SystemTodoFormDetail originForm, AbstractStateFormInput form, String message) throws Exception {
            SystemTodoFormDetail closedForm = getForm(form.getId());
            SystemNotifyService.sendAsync(
                    "system.todo.notify.standard.created", 
                    new ObjectMap()
                        .put("notifyService", getInstance())
                        .put("originForm", originForm)
                        .put("changedForm", closedForm)
                        .put("notifyInfo", getNotifyInfo(closedForm))
                        .asMap(), 0);
            return null;
        }
    }
    
    public class EventWhenClosed extends AbstractStateLeaveAction<SystemTodoFormDetail> {
        
        public EventWhenClosed () {
            super("待办事项关闭通知", STATES.OPENED.getCode());
        }
        
        @Override
        public Void handle(String event, SystemTodoFormDetail originForm, AbstractStateFormInput form, String message) throws Exception {
            SystemTodoFormDetail closedForm = getForm(form.getId());
            SystemNotifyService.sendAsync(
                    "system.todo.notify.standard.closed", 
                    new ObjectMap()
                        .put("notifyService", getInstance())
                        .put("originForm", originForm)
                        .put("changedForm", closedForm)
                        .put("notifyInfo", getNotifyInfo(closedForm))
                        .asMap(), 0);
            return null;
        }
    }
    
    public class EventEidt extends AbstractStateAction<SystemTodoFormDetail, SystemTodoFormEdition, Void> {
        
        public EventEidt () {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoFormDetail originForm, final SystemTodoFormEdition form, final String message)
                            throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    ObjectMap updated = new ObjectMap()
                        .put("=id", form.getId())
                        .put("title", form.getTitle())
                        .put("category", form.getCategory())
                        .put("target_key", form.getTargetKey())
                        .put("target_id", form.getTargetId())
                        .put("target_page", form.getTargetPage());
                    if (form.getApplyUser() != null) {
                        AbstractUser applier;
                        if ((applier = SystemUserService.getInstance().getSimple(form.getApplyUser().getId())) != null) {
                            updated.put("apply_user_id", applier.getId());
                            updated.put("apply_user_name", applier.getUsername());
                            updated.put("apply_user_display", applier.getDisplay());
                        }
                    }
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), updated));
                    setAssignees(form.getId(), form.getAssignees());
                }
            });
            return null;
        }
    }
    
    public class EventClose extends AbstractStateAction<SystemTodoFormDetail, SystemTodoFormClose, Void> {
        
        public EventClose() {
            super("关闭", STATES.OPENED.getCode(), STATES.CLOSED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = AuthorityEveryoneChecker.class)
        public void check(String event, SystemTodoFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoFormDetail originForm, final SystemTodoFormClose form, final String message)
                        throws Exception {
            String result;
            if ((result = form.getResult()).length() > 128) {
                result = result.substring(0, 128);
            }
            long closedUserId = SessionContext.getUserId();
            String closedUserName = SessionContext.getUsername();
            String closedUserDisplay = SessionContext.getDisplay();
            if (form.getClosedUserId() != null) {
                OptionSystemUser customUser;
                if ((customUser = ClassUtil.getSingltonInstance(FieldSystemUser.class)
                        .queryDynamicValue(form.getClosedUserId())) == null) {
                    throw new MessageException(
                            String.format("提供的用户信息(closedUserId = %s)不存在，请再次确认!", form.getClosedUserId()));
                }
                closedUserId = customUser.getId();
                closedUserName = customUser.getUsername();
                closedUserDisplay = customUser.getDisplay();
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                getFormTable(), new ObjectMap()
                        .put("=target_key", originForm.getTargetKey())
                        .put("=target_id", originForm.getTargetId())
                        .put(String.format("=%s", getFormStateField()), STATES.OPENED.getCode())
                        .put(getFormStateField(), STATES.CLOSED.getCode())
                        .put("result", result)
                        .put("closed_user_id", closedUserId)
                        .put("closed_user_name", closedUserName)
                        .put("closed_user_display", closedUserDisplay)
                        .put("closed_at", new Date())
            ));
            return null;
        }
    }
    
    public class EventReopen extends AbstractStateAction<SystemTodoFormDetail, StateFormBasicInput, Void> {
        
        public EventReopen() {
            super("恢复", STATES.CLOSED.getCode(), STATES.OPENED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoFormDetail form, String sourceState) {
            
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemTodoFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoFormDetail originForm, StateFormBasicInput form, String message)
                        throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            setAssignees(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        
        OnCreated(EventaWhenCreated.class),
        
        OnClosed(EventWhenClosed.class),
        
        Edit(EventEidt.class),
        
        Close(EventClose.class),
        
        Reopen(EventReopen.class),
        
        Delete(EventDelete.class);
        
        private final Class<? extends AbstractStateAction<SystemTodoFormDetail, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemTodoFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_todo";
    }
    
    @Override
    public String getFormTable() {
        return "system_todo";
    }
    
    public String getAssigneeTable() {
        return "system_todo_assignee";
    }
    
    @Override
    public String getFormDisplay() {
        return "代办事项";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Data
    @Accessors(chain = true)
    public static class TodoNotifyInfo {
        
        private SystemUserFormWithSecurity applierNotifyInfo;
        
        private SystemUserFormWithSecurity creatorNotifyInfo;
        
        private SystemUserFormWithSecurity closerNotifyInfo;
        
        private Collection<SystemUserFormWithSecurity> assigneeNotifyInfos;
    }
    
    private TodoNotifyInfo getNotifyInfo(SystemTodoFormDetail originForm) throws Exception {
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.add(originForm.getApplyUserId());
        allUserIds.add(originForm.getClosedUserId());
        allUserIds.add(originForm.getCreatedBy());
        List<OptionSystemUser> assignee;
        Set<Long> allAssigneeIds = new HashSet<>();
        if ((assignee = originForm.getAssignees()) != null) {
            for (OptionSystemUser o : assignee) {
                allUserIds.add(o.getId());
                allAssigneeIds.add(o.getId());
            }
        }
        List<SystemUserFormWithSecurity> usersSecurity = SystemUserService.getInstance().getUsersSecurity(
                        allUserIds.toArray(new Long[0]));
        Map<Long, SystemUserFormWithSecurity> allNotifyInfos = new HashMap<>();
        for (SystemUserFormWithSecurity u : usersSecurity) {
            allNotifyInfos.put(u.getId(), u);
            
        }
        TodoNotifyInfo result = new TodoNotifyInfo()
                .setApplierNotifyInfo(allNotifyInfos.get(originForm.getApplyUserId()))
                .setCloserNotifyInfo(allNotifyInfos.get(originForm.getClosedUserId()))
                .setCreatorNotifyInfo(allNotifyInfos.get(originForm.getCreatedBy()));
        for (Object userId : allNotifyInfos.keySet().toArray()) {
            if (!allAssigneeIds.contains(userId)) {
                allNotifyInfos.remove(userId);
            }
        }
        return result.setAssigneeNotifyInfos(allNotifyInfos.values());
    }
    
    private void setAssignees(long formId, List<OptionSystemUser> assignee) throws Exception {
        if (assignee == null) {
            return;
        }
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                getAssigneeTable(), new ObjectMap().put("=todo_id", formId)));
        for (OptionSystemUser user : assignee) {
            if (user == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getAssigneeTable(), new ObjectMap()
                            .put("=todo_user", user.getId())
                            .put("todo_id", formId)));
        }
    }
    
    private SystemTodoQueryDefault getOpenedByAssigneeQuery(Long assignee) {
        return new SystemTodoQueryDefault(1, 1000).setAssignee(assignee).setState(STATES.OPENED.getCode());
    }

    private SystemTodoQueryDefault getTodoByCreatorQuery(Long createdBy , Integer page , Integer limit) {
        return new SystemTodoQueryDefault(page, limit).setCreatedBy(createdBy);
    }

    private SystemTodoQueryDefault getTodoByCloserQuery(Long closedUserId , Integer page , Integer limit) {
        return new SystemTodoQueryDefault(page, limit).setClosedUserId(closedUserId);
    }
    
    @SuppressWarnings("unchecked")
    public List<SystemTodoFormDefault> queryOpenedByAssignee(Long assignee) throws Exception {
        if (assignee == null) {
            return Collections.emptyList();
        }
        return (List<SystemTodoFormDefault>) listForm(getFormDefaultQuery(), getOpenedByAssigneeQuery(assignee)).getList();
    }
    
    public long queryOpenedCountByAssignee(Long assignee) throws Exception {
        if (assignee == null) {
            return 0;
        }
        return getListFormTotal(getFormDefaultQuery(), getOpenedByAssigneeQuery(assignee));
    }
    
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<SystemTodoFormDefault> queryTodoByCreator(Long createdUserId , Integer page , Integer limit) throws Exception {
        return (PagedListWithTotal<SystemTodoFormDefault>) listFormWithTotal(getFormDefaultQuery(), getTodoByCreatorQuery(createdUserId , page , limit));
    }
    
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<SystemTodoFormDefault> queryTodoByCloser(Long closedUserId ,Integer page , Integer limit) throws Exception {
        return (PagedListWithTotal<SystemTodoFormDefault>) listFormWithTotal(getFormDefaultQuery(), getTodoByCloserQuery(closedUserId , page , limit));
    }
    
    @SuppressWarnings("unchecked")
    public List<SystemTodoFormDefault> queryClosedByCategoryId(String category, String targetId)
            throws Exception {
        if (StringUtils.isBlank(category) || StringUtils.isBlank(targetId)) {
            return Collections.emptyList();
        }
        return (List<SystemTodoFormDefault>) listForm(getFormDefaultQuery(), new SystemTodoQueryDefault(1, 100)
                .setCategory(category).setTargetId(targetId).setState(STATES.OPENED.getCode())).getList();
    }
    
    @SuppressWarnings("unchecked")
    public List<SystemTodoFormDefault> queryOpenedByTargetId(String targetKey, String targetId)
            throws Exception {
        if (StringUtils.isBlank(targetKey) || StringUtils.isBlank(targetId)) {
            return Collections.emptyList();
        }
        return (List<SystemTodoFormDefault>) listForm(getFormDefaultQuery(), new SystemTodoQueryDefault(1, 100)
                .setTargetKey(targetKey).setTargetId(targetId).setState(STATES.OPENED.getCode())).getList();
    }
    
    public SystemTodoFormDetail queryTodoId(Long id) throws Exception {
        if (id == null) {
            return null;
        }
        return getForm(id);
    }
    
    @Data
    public static class TodoAssigneeEntity {
        
        private long todoId;
        
        private long todoUser;
        
    }
    
    /**
     SELECT DISTINCT
         a.todo_id
         a.todo_user
     FROM
          %s a
     WHERE
         a.todo_id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_TODO_ASSIGNEE = "X";
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemTodoFormSimple> forms) throws Exception {
        
        if (forms == null || forms.size() <= 0) {
            return;
        }
        List<SystemTodoFormSimple> sameForms;
        Map<Long, List<SystemTodoFormSimple>> withAssignees = new HashMap<>();
        for (SystemTodoFormSimple form : forms) {
            if (form.getId() != null && (form instanceof SystemTodoFormWithAssignees)) {
                if ((sameForms = withAssignees.get(form.getId())) == null) {
                    withAssignees.put(form.getId(), sameForms = new ArrayList<>());
                }
                sameForms.add(form);
            }
        }
        if (withAssignees.size() > 0) {
            List<TodoAssigneeEntity> flattedAssignees = getFormBaseDao().queryAsList(
                    TodoAssigneeEntity.class,
                    String.format(SQL_QUERY_TODO_ASSIGNEE, getAssigneeTable(),
                            CommonUtil.join("?", withAssignees.size(), ",")),
                    withAssignees.keySet().toArray());
            
            List<Long> oneAssignees;
            Set<Long> allAssigneeIds = new HashSet<>();
            Map<Long, List<Long>> mappedAssigneeIds = new HashMap<>();
            for (TodoAssigneeEntity a : flattedAssignees) {
                allAssigneeIds.add(a.getTodoUser());
                if ((oneAssignees = mappedAssigneeIds.get(a.getTodoId())) == null) {
                    mappedAssigneeIds.put(a.getTodoId(), oneAssignees = new ArrayList<>());
                }
                oneAssignees.add(a.getTodoUser());
            }
            
            Map<Long, OptionSystemUser> mappedAllAssigneeEntities = new HashMap<>();
            List<OptionSystemUser> flattedAssigneeEntities = ClassUtil.getSingltonInstance(FieldSystemUser.class)
                    .queryDynamicValues(allAssigneeIds.toArray());
            for (OptionSystemUser o : flattedAssigneeEntities) {
                mappedAllAssigneeEntities.put(o.getId(), o);
            }
            
            List<OptionSystemUser> sameTodoAssigneeEntities;
            Map<Long, List<OptionSystemUser>> mappedSameAssigneeEntities = new HashMap<>();
            for (Map.Entry<Long, List<Long>> e : mappedAssigneeIds.entrySet()) {
                mappedSameAssigneeEntities.put(e.getKey(),
                        sameTodoAssigneeEntities = new ArrayList<>(e.getValue().size()));
                for (Long o : e.getValue()) {
                    if (o != null && mappedAllAssigneeEntities.containsKey(o)) {
                        sameTodoAssigneeEntities.add(mappedAllAssigneeEntities.get(o));
                    }
                }
            }
            
            for (Map.Entry<Long, List<SystemTodoFormSimple>> e : withAssignees.entrySet()) {
                for (SystemTodoFormSimple form : e.getValue()) {
                    ((SystemTodoFormWithAssignees) form).setAssignees(mappedSameAssigneeEntities.get(e.getKey()));
                }
            }
        }
        
    }
}
