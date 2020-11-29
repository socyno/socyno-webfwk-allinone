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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityEveryoneChecker;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateEnterAction;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateLeaveAction;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserSecurityOnly;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
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

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

public class SystemTodoService extends AbstractStateFormServiceWithBaseDao<SystemTodoFormDetail> {

    public static final SystemTodoService DEFAULT = new SystemTodoService();
    
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
        
        public static String[] stringify(STATES... states) {
            if (states == null || states.length <=0 ) {
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
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemTodoDefaultForm>("default", 
                SystemTodoDefaultForm.class, SystemTodoDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Create(new AbstractStateSubmitAction<SystemTodoFormDetail, SystemTodoFormForCreation>("添加", STATES.OPENED.getCode()) {
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
            public Long handle(String event, SystemTodoFormDetail originForm, SystemTodoFormForCreation form, String message) throws Exception {
                final AtomicLong todoId = new AtomicLong();
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
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
                            if ((applier = SystemUserService.DEFAULT.getSimple(form.getApplyUser().getId())) != null) {
                                applyUserId = applier.getId();
                                applyUsername = applier.getUsername();
                                applyDisplay = applier.getDisplay();
                            }
                        }
                        DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            DEFAULT.getFormTable(), new ObjectMap()
                                    .put("title", StringUtils.truncate(form.getTitle(), 128))
                                    .put("category", form.getCategory())
                                    .put("target_key", form.getTargetKey())
                                    .put("target_id", form.getTargetId())
                                    .put("target_page", targetPage)
                                    .put("apply_user_id", applyUserId)
                                    .put("apply_user_name", applyUsername)
                                    .put("apply_user_display", applyDisplay)
                                    .put("created_user_id", SessionContext.getTokenUserId())
                                    .put("created_user_name", SessionContext.getTokenUsername())
                                    .put("created_user_display", SessionContext.getTokenDisplay())
                                    .put("created_at", new Date())
                        ), new ResultSetProcessor() {
                            @Override
                            public void process(ResultSet r, Connection c) throws Exception {
                                r.next();
                                todoId.set(r.getLong(1));
                            }
                        });
                        setAssignee(todoId.get(), form.getAssignee());
                    }
                });
                return todoId.get();
            }
        }),
        
        OnCreated(new AbstractStateEnterAction<SystemTodoFormDetail>("待办事项创建内部事件", STATES.OPENED.getCode()) {
            @Override
            public Void handle(String event, SystemTodoFormDetail originForm, AbstractStateForm form, String message) throws Exception {
                SystemTodoFormDetail closedForm = DEFAULT.getForm(form.getId());
                SystemNotifyService.sendAsync(
                        "system.todo.notify.standard.created", 
                        new ObjectMap()
                            .put("notifyService", DEFAULT)
                            .put("originForm", originForm)
                            .put("changedForm", closedForm)
                            .put("notifyInfo", DEFAULT.getNotifyInfo(closedForm))
                            .asMap(), 0);
                return null;
            }
        }),
        
        OnClosed(new AbstractStateLeaveAction<SystemTodoFormDetail>("待办事项关闭内部事件", STATES.OPENED.getCode()) {
            @Override
            public Void handle(String event, SystemTodoFormDetail originForm, AbstractStateForm form, String message) throws Exception {
                SystemTodoFormDetail closedForm = DEFAULT.getForm(form.getId());
                SystemNotifyService.sendAsync(
                        "system.todo.notify.standard.closed", 
                        new ObjectMap()
                            .put("notifyService", DEFAULT)
                            .put("originForm", originForm)
                            .put("changedForm", closedForm)
                            .put("notifyInfo", DEFAULT.getNotifyInfo(closedForm))
                            .asMap(), 0);
                return null;
            }
        }),
        
        Edit(new AbstractStateAction<SystemTodoFormDetail, SystemTodoFormForEdition, Void>("编辑", STATES.stringifyEx(), "") {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTodoFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, SystemTodoFormDetail originForm, final SystemTodoFormForEdition form, final String message)
                                throws Exception {
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
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
                            if ((applier = SystemUserService.DEFAULT.getSimple(form.getApplyUser().getId())) != null) {
                                updated.put("apply_user_id", applier.getId());
                                updated.put("apply_user_name", applier.getUsername());
                                updated.put("apply_user_display", applier.getDisplay());
                            }
                        }
                        DEFAULT.getFormBaseDao()
                                .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(), updated));
                        setAssignee(form.getId(), form.getAssignee());
                    }
                });
                return null;
            }
        }),
        Close(new AbstractStateAction<SystemTodoFormDetail, SystemTodoFormForClose, Void>("关闭",
                            STATES.OPENED.getCode(), STATES.CLOSED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System, checker = AuthorityEveryoneChecker.class)
            public void check(String event, SystemTodoFormDetail form, String sourceState) {
                
            }
            
            
            @Override
            public Void handle(String event, SystemTodoFormDetail originForm, final SystemTodoFormForClose form, final String message)
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
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    DEFAULT.getFormTable(), new ObjectMap()
                            .put("=target_key", originForm.getTargetKey())
                            .put("=target_id", originForm.getTargetId())
                            .put(String.format("=%s", DEFAULT.getFormStateField()), STATES.OPENED.getCode())
                            .put(DEFAULT.getFormStateField(), STATES.CLOSED.getCode())
                            .put("result", result)
                            .put("closed_user_id", closedUserId)
                            .put("closed_user_name", closedUserName)
                            .put("closed_user_display", closedUserDisplay)
                            .put("closed_at", new Date())
                ));
                return null;
            }
        }),
        Reopen(new AbstractStateAction<SystemTodoFormDetail, BasicStateForm, Void>("恢复",
                            STATES.CLOSED.getCode(), STATES.OPENED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTodoFormDetail form, String sourceState) {
                
            }
        })
        , Delete(new AbstractStateDeleteAction<SystemTodoFormDetail>("删除", STATES.stringifyEx()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTodoFormDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, SystemTodoFormDetail originForm, BasicStateForm form, String message)
                            throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        DEFAULT.getFormTable(), new ObjectMap()
                                .put("=id", originForm.getId())
                ));
                setAssignee(originForm.getId(), Collections.emptyList());
                return null;
            }
        })
        ;
        
        private final AbstractStateAction<SystemTodoFormDetail, ?, ?> action;
        EVENTS(AbstractStateAction<SystemTodoFormDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SystemTodoFormDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    @Override
    public String getFormName() {
        return getName();
    }
    
    @Override
    public String getFormTable() {
        return getTable();
    }
    
    protected static String getTable() {
        return "system_common_todo";
    }
    
    protected static String getName() {
        return "system_common_todo";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemTodoFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemTodoFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Data
    @Accessors(chain = true)
    public static class TodoNotifyInfo {
        
        private SystemUserSecurityOnly applierNotifyInfo;
        
        private SystemUserSecurityOnly creatorNotifyInfo;
        
        private SystemUserSecurityOnly closerNotifyInfo;
        
        private Collection<SystemUserSecurityOnly> assigneeNotifyInfos;
    }
    
    private TodoNotifyInfo getNotifyInfo(SystemTodoFormDetail originForm) throws Exception {
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.add(originForm.getApplyUserId());
        allUserIds.add(originForm.getClosedUserId());
        allUserIds.add(originForm.getCreatedUserId());
        List<OptionSystemUser> assignee;
        Set<Long> allAssigneeIds = new HashSet<>();
        if ((assignee = originForm.getAssignee()) != null) {
            for (OptionSystemUser o : assignee) {
                allUserIds.add(o.getId());
                allAssigneeIds.add(o.getId());
            }
        }
        List<SystemUserSecurityOnly> usersSecurity = SystemUserService.DEFAULT.getUsersSecurity(
                        allUserIds.toArray(new Long[0]));
        Map<Long, SystemUserSecurityOnly> allNotifyInfos = new HashMap<>();
        for (SystemUserSecurityOnly u : usersSecurity) {
            allNotifyInfos.put(u.getId(), u);
            
        }
        TodoNotifyInfo result = new TodoNotifyInfo()
                .setApplierNotifyInfo(allNotifyInfos.get(originForm.getApplyUserId()))
                .setCloserNotifyInfo(allNotifyInfos.get(originForm.getClosedUserId()))
                .setCreatorNotifyInfo(allNotifyInfos.get(originForm.getCreatedUserId()));
        for (Object userId : allNotifyInfos.keySet().toArray()) {
            if (!allAssigneeIds.contains(userId)) {
                allNotifyInfos.remove(userId);
            }
        }
        return result.setAssigneeNotifyInfos(allNotifyInfos.values());
    }
    
    /**
     SELECT DISTINCT
         a.todo_user
     FROM
         system_common_todo_assignee a
     WHERE
         a.todo_id = ?
     */
    @Multiline
    private static final String SQL_QUERY_TODO_ASSIGNEE = "X";
    
    private static SystemTodoFormDetail withAssignee(SystemTodoFormDetail form) throws Exception {
        if (form == null) {
            return null;
        }
        if (form.getId() != null) {
            List<Long> assignee = getDao().queryAsList(Long.class, String.format(SQL_QUERY_TODO_ASSIGNEE, getTable()),
                    new Object[] { form.getId() });
            
            form.setAssignee(ClassUtil.getSingltonInstance(FieldSystemUser.class)
                    .queryDynamicValues(assignee.toArray(new Long[0])));
        }
        return form;
    }
    
    /**
     * 获取待办事项的详情数据。
     */
    @Override
    public SystemTodoFormDetail getForm(long id) throws Exception {
        return withAssignee(super.getForm(id));
    }
    
    private static void setAssignee(long formId, List<OptionSystemUser> assignee) throws Exception {
        if (assignee == null) {
            return;
        }
        DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_common_todo_assignee", new ObjectMap().put("=todo_id", formId)));
        for (OptionSystemUser user : assignee) {
            if (user == null) {
                continue;
            }
            DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "system_common_todo_assignee", new ObjectMap()
                            .put("=todo_user", user.getId())
                            .put("todo_id", formId)));
        }
    }
    
    private static SystemTodoDefaultQuery getOpenedByAssigneeQuery(Long assignee) {
        return new SystemTodoDefaultQuery(1, 1000).setAssignee(assignee).setState(STATES.OPENED.getCode());
    }

    private static SystemTodoDefaultQuery getTodoByCreatorQuery(Long createdUserId , Integer page , Integer limit) {
        return new SystemTodoDefaultQuery(page, limit).setCreatedUserId(createdUserId);
    }

    private static SystemTodoDefaultQuery getTodoByCloserQuery(Long closedUserId , Integer page , Integer limit) {
        return new SystemTodoDefaultQuery(page, limit).setClosedUserId(closedUserId);
    }
    
    @SuppressWarnings("unchecked")
    public static List<SystemTodoDefaultForm> queryOpenedByAssignee(Long assignee) throws Exception {
        if (assignee == null) {
            return Collections.emptyList();
        }
        return (List<SystemTodoDefaultForm>) DEFAULT.listForm("default", getOpenedByAssigneeQuery(assignee)).getList();
    }
    
    public static long queryOpenedCountByAssignee(Long assignee) throws Exception {
        if (assignee == null) {
            return 0;
        }
        return DEFAULT.getListFormTotal("default", getOpenedByAssigneeQuery(assignee));
    }
    
    @SuppressWarnings("unchecked")
    public static PagedListWithTotal<SystemTodoDefaultForm> queryTodoByCreator(Long createdUserId , Integer page , Integer limit) throws Exception {
        return (PagedListWithTotal<SystemTodoDefaultForm>) DEFAULT.listFormWithTotal("default", getTodoByCreatorQuery(createdUserId , page , limit));
    }
    
    @SuppressWarnings("unchecked")
    public static PagedListWithTotal<SystemTodoDefaultForm> queryTodoByCloser(Long closedUserId ,Integer page , Integer limit) throws Exception {
        return (PagedListWithTotal<SystemTodoDefaultForm>) DEFAULT.listFormWithTotal("default", getTodoByCloserQuery(closedUserId , page , limit));
    }
    
    @SuppressWarnings("unchecked")
    public static List<SystemTodoDefaultForm> queryClosedByCategoryId(String category, String targetId)
            throws Exception {
        if (StringUtils.isBlank(category) || StringUtils.isBlank(targetId)) {
            return Collections.emptyList();
        }
        return (List<SystemTodoDefaultForm>) DEFAULT.listForm("default", new SystemTodoDefaultQuery(1, 100)
                .setCategory(category).setTargetId(targetId).setState(STATES.OPENED.getCode())).getList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<SystemTodoDefaultForm> queryOpenedByTargetId(String targetKey, String targetId)
            throws Exception {
        if (StringUtils.isBlank(targetKey) || StringUtils.isBlank(targetId)) {
            return Collections.emptyList();
        }
        return (List<SystemTodoDefaultForm>) DEFAULT.listForm("default", new SystemTodoDefaultQuery(1, 100)
                .setTargetKey(targetKey).setTargetId(targetId).setState(STATES.OPENED.getCode())).getList();
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    
    public static SystemTodoFormDetail queryTodoId(Long id) throws Exception {
        if (id == null) {
            return null;
        }
        return DEFAULT.getForm(id);
    }
}
