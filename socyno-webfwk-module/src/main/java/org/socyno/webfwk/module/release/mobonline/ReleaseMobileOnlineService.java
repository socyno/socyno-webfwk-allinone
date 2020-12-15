package org.socyno.webfwk.module.release.mobonline;

import lombok.Getter;
import lombok.SneakyThrows;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateChoice;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.AbstractStateTodoApprovalAction;
import org.socyno.webfwk.state.basic.AbstractStateTodoCloseAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.model.CommonAttachementItem;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.SystemUserFormSimple;
import org.socyno.webfwk.state.service.AttachmentService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.remote.R;
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

public class ReleaseMobileOnlineService extends
        AbstractStateFormServiceWithBaseDao<ReleaseMobileOnlineFormDetail, ReleaseMobileOnlineFormDefault, ReleaseMobileOnlineFormSimple> {
    
    private ReleaseMobileOnlineService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final ReleaseMobileOnlineService Instance = new ReleaseMobileOnlineService();

    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "待审批"),
        APPROVAL_REJECT("approval_reject", "审批拒绝"),
        WAIT_SPECIAL_APPROVER("wait_special_approver","待特殊审批人审批"),
        SPECIAL_APPROVER_REJECT("special_approver_reject","特殊审批人拒绝"),
        EXECUTION("execution","执行上架"),
        END("end","结束");

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
         * 废除
         */
        Abolition(EventAbolition.class),
        /**
         * 补充附件
         */
        supplementaryAttachment(EventSupplementaryAttachment.class),
        storeStatusOperations(EventsStoreStatusOperations.class),

        ApprovalPassed(EventApprovalPassed.class),
        ApprovalReject(EventApprovalReject.class),
        SpecialApprovalPassed(EventSpecialApprovalPassed.class),
        SpecialApprovalReject(EventSpecialApprovalReject.class),

        ReleaseComplete(EventReleaseComplete.class),

        SendEmail(EventSendEmail.class),

        ApprovalCreate(EventApprovalCreate.class),
        ApprovalClose(EventApprovalClose.class),
        SpecialApprovalCreate(EventSpecialApprovalCreate.class),
        SpecialApprovalClose(EventSpecialApprovalClose.class);


        private final Class<? extends AbstractStateAction<ReleaseMobileOnlineFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<ReleaseMobileOnlineFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ReleaseMobileOnlineFormDefault>("默认查询", ReleaseMobileOnlineFormDefault.class,
                ReleaseMobileOnlineDefaultQuery.class));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends ReleaseMobileOnlineFormSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
        
        Map<Long, ReleaseMobileOnlineWithAttachements> withAttachements = new HashMap<>();
        Map<Long, ReleaseMobileOnlineWithAppStore> withStore = new HashMap<>();
        for (ReleaseMobileOnlineFormSimple form : forms) {
            if (form == null) {
                continue;
            }
            if (ReleaseMobileOnlineWithAttachements.class.isAssignableFrom(form.getClass())) {
                withAttachements.put(form.getId(), (ReleaseMobileOnlineWithAttachements) form);
            }
            if (ReleaseMobileOnlineWithAppStore.class.isAssignableFrom(form.getClass())) {
                withStore.put(form.getId(), form);
            }
            
            List<OptionReleaseMobileOnlineAppStore> storeList = queryItemStore(form.getId());
            if (storeList != null && storeList.size() > 0) {
                List<FieldReleaseMobileOnlineAppStore.OptionStore> option = new ArrayList<>();
                List<String> storeArr = new ArrayList<>();
                for (OptionReleaseMobileOnlineAppStore itemStore : storeList) {
                    FieldReleaseMobileOnlineAppStore.OptionStore optionStore = new FieldReleaseMobileOnlineAppStore.OptionStore();
                    optionStore.setStoreName(itemStore.getStoreName());
                    option.add(optionStore);
                    storeArr.add(itemStore.getStoreName());
                }
                String store = String.format(CommonUtil.join("%s", storeList.size(), ","), storeArr.toArray());
                form.setStore(option.toArray(new FieldReleaseMobileOnlineAppStore.OptionStore[0]));
                form.setAppStore(store);
            }
        }
        
        if (withAttachements.size() > 0) {
            List<CommonAttachementItem> attachements = AttachmentService.queryByTargetFormFeild(getFormName(),
                    "attachements", withAttachements.keySet().toArray());
            Map<Long, List<CommonAttachementItem>> allFormAttachements = new HashMap<>();
            List<CommonAttachementItem> singleFormAttachements;
            for (CommonAttachementItem option : attachements) {
                if ((singleFormAttachements = allFormAttachements.get(option.getFormId())) == null) {
                    allFormAttachements.put(option.getFormId(), singleFormAttachements = new ArrayList<>());
                }
                singleFormAttachements.add(option);
            }
            for (Map.Entry<Long, ReleaseMobileOnlineWithAttachements> entry : withAttachements.entrySet()) {
                Long formId = entry.getKey();
                entry.getValue().setAttachements(allFormAttachements.get(formId));
            }
        }
        
        if (withStore.size() > 0) {
            List<OptionReleaseMobileOnlineAppStore> ports = queryItemStoreIds(withStore.keySet().toArray(new Long[0]));
            Map<Long, List<OptionReleaseMobileOnlineAppStore>> stores = ports.stream()
                    .collect(Collectors.groupingBy(OptionReleaseMobileOnlineAppStore::getReleaseAppStoreId));
            for (ReleaseMobileOnlineFormSimple form : forms) {
                if (form.getItemStore() == null) {
                    form.setItemStore(stores.get(form.getId()));
                }
            }
        }
    }
    
    /**
     * select * from release_app_status where release_app_store_id in (%s)
     */
    @Multiline
    private final static String SQL_QUERY_STORE_BY_ID = "X";
    
    public List<OptionReleaseMobileOnlineAppStore> queryItemStoreIds(Long... Ids) throws Exception {
        if (Ids == null || Ids.length <= 0) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(OptionReleaseMobileOnlineAppStore.class,
                String.format(SQL_QUERY_STORE_BY_ID, CommonUtil.join("?", Ids.length, ",")), Ids);
    }
    
    @Override
    protected String getFormTable() {
        return "release_app_online";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "release_app_store";
    }
    
    @Override
    public String getFormDisplay() {
        return "市场发布";
    }
    
    public static class userChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null && SessionContext.getUserId().equals(((ReleaseMobileOnlineFormSimple) form).getCreatedBy());
        }
        
    }
    
    public static class approveUserChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((ReleaseMobileOnlineFormSimple) form).getApprover().getId());
        }
        
    }
    
    public static class specialApproveUserChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((ReleaseMobileOnlineFormSimple) form).getSpecialApprover().getId());
        }
        
    }
    
    public class EventCreate extends AbstractStateSubmitAction<ReleaseMobileOnlineFormSimple, ReleaseMobileOnlineFormCreate> {
        
        public EventCreate() {
            super("申请", STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileOnlineFormSimple form, String message) {
            
        }
        
        @Override
        public Long handle(String event, ReleaseMobileOnlineFormSimple originForm, ReleaseMobileOnlineFormCreate form, String message)
                throws Exception {
            OptionReleaseMobileOnlineApplication releaseAppStorDeploy = queryConfigurationInformation(
                    form.getApplicationName());
            if ("ios".equals(releaseAppStorDeploy.getStoreType())) {
                if (!"ios".equals(form.getStore()[0].getStoreName())) {
                    throw new MessageException("ios系统仅可选ios商店");
                }
            } else if ("android".equals(releaseAppStorDeploy.getStoreType())) {
                if ("ios".equals(form.getStore()[0].getStoreName())) {
                    throw new MessageException("android系统仅可选android商店");
                }
            }
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getUsername())
                            .put("created_name_by", SessionContext.getDisplay())
                            .put("application_name", form.getApplicationName())
                            .put("release", form.getRelease())
                            .put("release_note", form.getReleaseNote())
                            .put("store_type", releaseAppStorDeploy.getStoreType())
                            .put("approver", releaseAppStorDeploy.getApprover())
                            .put("special_approver", form.getSpecialApprover())
            ), new AbstractDao.ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    resultSet.next();
                    id.set(resultSet.getLong(1));
                    handleAttachments(form, id.get());
                }
            });
            saveAppStore(id.get(), form.getStore());
            return id.get();
        }
    }
    
    // 多个附件上传,创建
    private void handleAttachments(ReleaseMobileOnlineWithAttachements form, Long id) throws Exception {
        List<CommonAttachementItem> upload;
        if ((upload = form.getAttachements()) != null) {
            Set<Long> attachementIds = new HashSet<>();
            for (CommonAttachementItem item : upload) {
                attachementIds.add(item.getId());
            }
            AttachmentService.cleanByTargetFormField(getFormName(), id, "attachements");
            AttachmentService.bindWithForm(getFormName(), id, attachementIds.toArray(new Long[0]));
        }
    }
    
    public class EventEdit extends AbstractStateAction<ReleaseMobileOnlineFormSimple, ReleaseMobileOnlineFormUpdate, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodes(STATES.APPROVAL_REJECT, STATES.SPECIAL_APPROVER_REJECT),
                    STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = userChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, ReleaseMobileOnlineFormUpdate form,
                String message) throws Exception {
            OptionReleaseMobileOnlineApplication releaseAppStorDeploy = queryConfigurationInformation(
                    form.getApplicationName());
            if (releaseAppStorDeploy == null) {
                throw new MessageException("缺乏应用配置");
            }
            if ("ios".equals(releaseAppStorDeploy.getStoreType())) {
                if (!"ios".equals(form.getStore()[0].getStoreName())) {
                    throw new MessageException("ios系统仅可选ios商店");
                }
            } else if ("android".equals(releaseAppStorDeploy.getStoreType())) {
                if ("ios".equals(form.getStore()[0].getStoreName())) {
                    throw new MessageException("android系统仅可选android商店");
                }
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("application_name", form.getApplicationName())
                            .put("release", form.getRelease())
                            .put("release_note", form.getReleaseNote())
                            .put("store_type", releaseAppStorDeploy.getStoreType())
                            .put("approver", releaseAppStorDeploy.getApprover())
                            .put("special_approver", form.getSpecialApprover())
            ));
            handleAttachments(form, form.getId());
            saveAppStore(form.getId(), form.getStore());
            return null;
        }
        
    }
    
    public class EventSupplementaryAttachment
            extends AbstractStateAction<ReleaseMobileOnlineFormSimple, ReleaseMobileOnlineFormUpload, Void> {
        
        public EventSupplementaryAttachment() {
            super("上架信息补充", getStateCodes(STATES.EXECUTION), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = userChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event , ReleaseMobileOnlineFormSimple originForm , ReleaseMobileOnlineFormUpload form , String message) throws Exception{
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("release_note", form.getReleaseNote())
            ));
            handleAttachments(form, form.getId());
            return null;
        }
        
    }
    
    public class EventsStoreStatusOperations
            extends AbstractStateAction<ReleaseMobileOnlineFormSimple, ReleaseMobileOnlineFormStatus, R> {
        
        public EventsStoreStatusOperations() {
            super("状态变更", getStateCodes(STATES.EXECUTION), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String message) {
            
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
        
        @Override
        public R handle(String event , ReleaseMobileOnlineFormSimple originForm , ReleaseMobileOnlineFormStatus form , String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    "release_app_status", new ObjectMap()
                            .put("=release_app_store_id", form.getId())
                            .put("=store_name", form.getStoreName())
                            .put("status", form.getStatus())
                            .put("comment", form.getComment())
                            .put("outer_time", form.getOuterTime())
                            .put("upload_time", form.getUploadTime())
            ));
            return R.ok().setData(null).setMessage("refresh");
        }
        
    }
    
    public class EventAbolition extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventAbolition() {
            super("废除", getStateCodes(STATES.APPROVAL_REJECT, STATES.SPECIAL_APPROVER_REJECT), STATES.END.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = userChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, String sourceState)
                throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId()).put("end_result", "撤销")));
            return null;
        }
    }
    
    public class EventApprovalPassed extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventApprovalPassed() {
            super("审批通过", getStateCodes(STATES.CREATED), new AbstractStateChoice("是否包含特殊审批人？",
                    STATES.WAIT_SPECIAL_APPROVER.getCode(), STATES.EXECUTION.getCode()) {
                @SneakyThrows
                @Override
                protected boolean select(AbstractStateForm abstractStateForm) {
                    Long specialApprover = getFormBaseDao().queryAsObject(Long.class,
                            "select special_approver from release_app_online where id = ?",
                            new Object[] { abstractStateForm.getId() });
                    if (specialApprover != null) {
                        return true;
                    }
                    return false;
                }
            });
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = approveUserChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
        
    }
    
    public class EventApprovalReject extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventApprovalReject() {
            super("审批拒绝", getStateCodes(STATES.CREATED), STATES.APPROVAL_REJECT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = approveUserChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
    }
    
    public class EventSpecialApprovalPassed extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventSpecialApprovalPassed() {
            super("审批通过(s)", getStateCodes(STATES.WAIT_SPECIAL_APPROVER), STATES.EXECUTION.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = specialApproveUserChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
        
    }
    
    public class EventSpecialApprovalReject extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventSpecialApprovalReject() {
            super("审批拒绝(s)", getStateCodes(STATES.WAIT_SPECIAL_APPROVER), STATES.SPECIAL_APPROVER_REJECT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = specialApproveUserChecker.class)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
    }
    
    public class EventReleaseComplete extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventReleaseComplete() {
            super("结束", getStateCodes(STATES.EXECUTION), STATES.END.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, final String message)
                throws Exception {
            Map<String, Integer> mapStatus = new HashMap<>();
            for (OptionReleaseMobileOnlineAppStore store : originForm.getItemStore()) {
                if (mapStatus.containsKey(store.getStatus())) {
                    Integer statusCount = mapStatus.get(store.getStatus()) + 1;
                    mapStatus.put(store.getStatus(), statusCount);
                    continue;
                }
                mapStatus.put(store.getStatus(), 1);
            }
            String endResult = "";
            if (!mapStatus.containsKey("uploadedComplete")) {
                endResult = "未上架";
            } else if (mapStatus.get("uploadedComplete") == originForm.getItemStore().size()) {
                endResult = "上架完成";
            } else if (mapStatus.get("uploadedComplete") < originForm.getItemStore().size()
                    && mapStatus.get("uploadedComplete") > 0) {
                endResult = "部分上架";
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId()).put("end_result", endResult)));
            return null;
        }
        
    }
    
    public class EventSendEmail extends AbstractStateAction<ReleaseMobileOnlineFormSimple, BasicStateForm, Void> {
        
        public EventSendEmail() {
            super("上架状态邮件通知", STATES.EXECUTION.getCode(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileOnlineFormSimple originForm, final String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileOnlineFormSimple originForm, BasicStateForm form, String message)
                throws Exception {
            operationNotification(event, originForm);
            return null;
        }
    }
    
    public class EventApprovalCreate extends AbstractStateTodoApprovalAction<ReleaseMobileOnlineFormSimple> {
        
        public EventApprovalCreate() {
            super("创建上架审批人待办事项", STATES.CREATED.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return String.format("%s:%s - %s", getFormDisplay(), getForm(form.getId()).getApplicationName(), "应用上架审批");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            SystemUserFormSimple systemUserSimple = SystemUserService.getInstance()
                    .getSimple(getForm(form.getId()).getApprover().getId());
            long[] scmUser = ConvertUtil
                    .asNonNullUniquePrimitiveLongArray(getActionUserIds(EVENTS.ReleaseComplete.getName(), originForm));
            Set<Long> userIds = new HashSet<>();
            userIds.add(systemUserSimple.getId());
            for (long userId : scmUser) {
                userIds.add(userId);
            }
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(userIds.toArray());
        }
        
        @Override
        protected String getNextRejectEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form) {
            return EVENTS.ApprovalReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form) {
            return EVENTS.ApprovalPassed.getName();
        }
    }
    
    public class EventApprovalClose extends AbstractStateTodoCloseAction<ReleaseMobileOnlineFormSimple> {
        
        public EventApprovalClose() {
            super("关闭上架审批人待办事项", STATES.CREATED.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return EVENTS.ApprovalCreate.getName();
        }
    }
    
    public class EventSpecialApprovalCreate extends AbstractStateTodoApprovalAction<ReleaseMobileOnlineFormSimple> {
        
        public EventSpecialApprovalCreate() {
            super("创建上架特殊审批人待办事项", STATES.WAIT_SPECIAL_APPROVER.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return String.format("%s:%s - %s", getFormDisplay(), getForm(form.getId()).getApplicationName(),
                    "应用上架审批-额外审批人");
        }
        
        @Override
        protected long[] getTodoAssignees(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            SystemUserFormSimple systemUserSimple = (SystemUserFormSimple) SystemUserService.getInstance()
                    .getSimple(getForm(form.getId()).getSpecialApprover().getId());
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(systemUserSimple.getId());
        }
        
        @Override
        protected String getNextRejectEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form) {
            return EVENTS.SpecialApprovalReject.getName();
        }
        
        @Override
        protected String getNextApproveEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form) {
            return EVENTS.SpecialApprovalPassed.getName();
        }
    }
    
    public class EventSpecialApprovalClose extends AbstractStateTodoCloseAction<ReleaseMobileOnlineFormSimple> {
        
        public EventSpecialApprovalClose() {
            super("关闭上架特殊审批人待办事项", STATES.WAIT_SPECIAL_APPROVER.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, ReleaseMobileOnlineFormSimple originForm, AbstractStateForm form)
                throws Exception {
            return EVENTS.SpecialApprovalCreate.getName();
        }
    }
    
    @Override
    public ReleaseMobileOnlineFormDetail getForm(long formId) throws Exception {
        return getForm(ReleaseMobileOnlineFormDetail.class, formId);
    }
    
    public OptionReleaseMobileOnlineApplication queryConfigurationInformation(String applicationName) throws Exception {
        return getFormBaseDao().queryAsObject(OptionReleaseMobileOnlineApplication.class,
                "SELECT * FROM `release_app_config` d where d.application_name = ? ", new Object[] { applicationName });
    }
    
    private void saveAppStore(Long id, FieldReleaseMobileOnlineAppStore.OptionStore[] store) throws Exception {
        Map<String, OptionReleaseMobileOnlineAppStore> optionMap = new HashMap<>();
        for (FieldReleaseMobileOnlineAppStore.OptionStore storeName : store) {
            OptionReleaseMobileOnlineAppStore itemStore = new OptionReleaseMobileOnlineAppStore();
            itemStore.setReleaseAppStoreId(id);
            itemStore.setStoreName(storeName.getStoreName());
            itemStore.setStatus("pending");
            itemStore.setComment(null);
            String channelName = getFormBaseDao().queryAsObject(String.class,
                    "SELECT channel_name FROM `release_app_store` where store_name = ? ",
                    new Object[] { storeName.getStoreName() });
            itemStore.setChannelName(channelName);
            optionMap.put(storeName.getStoreName(), itemStore);
        }
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("release_app_status",
                new ObjectMap().put("=release_app_store_id", id)));
        
        for (String s : optionMap.keySet()) {
            OptionReleaseMobileOnlineAppStore mergeStore = optionMap.get(s);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "release_app_status" , new ObjectMap()
                            .put("release_app_store_id" , mergeStore.getReleaseAppStoreId())
                            .put("store_name" , mergeStore.getStoreName())
                            .put("status" , mergeStore.getStatus())
                            .put("comment", mergeStore.getComment())
                            .put("channel_name", mergeStore.getChannelName())
            ));
        }
    }
    
    private List<OptionReleaseMobileOnlineAppStore> queryItemStore(Long id) throws Exception {
        return getFormBaseDao().queryAsList(OptionReleaseMobileOnlineAppStore.class,
                "SELECT * FROM `release_app_status` s where s.release_app_store_id = ? ", new Object[] { id });
    }
    
    /**
     * 操作通知发送
     *
     * @param event
     * @param originForm
     * @throws Exception
     */
    public void operationNotification(String event, ReleaseMobileOnlineFormSimple originForm) throws Exception {
        List<OptionSystemUser> users;
        Set<Long> userIds = new HashSet<>();
        userIds.add(originForm.getCreatedBy());
        userIds.add(originForm.getApprover().getId());
        if (originForm.getSpecialApprover() != null) {
            userIds.add(originForm.getSpecialApprover().getId());
        }
        long[] scmUser = ConvertUtil
                .asNonNullUniquePrimitiveLongArray(getActionUserIds(EVENTS.ReleaseComplete.getName(), originForm));
        for (long l : scmUser) {
            userIds.add(l);
        }
        users = ClassUtil.getSingltonInstance(FieldSystemUser.class).queryDynamicValues(userIds.toArray());
        
        List<String> emails = new ArrayList<>();
        for (OptionSystemUser user : users) {
            emails.add(user.getMailAddress());
        }
        
        SystemNotifyService.sendSync(
                "release.app.store.notice",
                new ObjectMap()
                        .put("event", event)
                        .put("originForm", originForm)
                        .put("addressee", emails)
                        .asMap(), 0);
    }
    
}
