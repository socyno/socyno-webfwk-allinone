package org.socyno.webfwk.module.release.change;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.NameValuePair;
import org.socyno.webfwk.module.application.FieldApplication.OptionApplication;
import org.socyno.webfwk.module.release.change.ChangeRequestFormSimple.Category;
import org.socyno.webfwk.module.release.change.ChangeRequestFormSimple.ChangeType;
import org.socyno.webfwk.module.release.change.ChangeRequestFormSimple.ScopeType;
import org.socyno.webfwk.module.release.change.FieldChangeRequestReleaseId.OptionReleaseId;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.modutil.SubsystemBasicUtil;
import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateChoice;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.abs.AbstractStateFormInput;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.abs.AbstractStatePrepare;
import org.socyno.webfwk.state.abs.AbstractStateTodoCloseAction;
import org.socyno.webfwk.state.abs.AbstractStateTodoCreateAction;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.state.model.CommonFormAttachement;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.service.AttachmentService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.sugger.SuggerDefinitionFormAttachment;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ChangeRequestService extends
        AbstractStateFormServiceWithBaseDao<ChangeRequestFormDetail, ChangeRequestFormSimple, ChangeRequestFormSimple> {
    
    private final static long MilliSecondsOf8Hours = 8 * 60 * 60000L;
    
    private ChangeRequestService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private final static ChangeRequestService Instance = new ChangeRequestService();
    
    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionChangeCategory.getInstance());
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionChangeReleaseId.getInstance());
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionChangeApplication.getInstance());
        // 注册附件表单名称
        SuggerDefinitionFormAttachment.addFormName(ChangeRequestFormSimple.class, Instance.getFormName());
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends ChangeRequestFormSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
//
//        Map<Long, AbsChangeDetail> changeDetail = new HashMap<>();
//        
//        for (ChangeRequestSimple form : forms) {
//            if (OtherChangeTypeDetail.class.isAssignableFrom(form.getClass())
//                    && ChangeType.MQ.getCode().equals(form.getChangeType())) {
//                kafkaDetail.put(form.getId(), (ChangeRequestDetail) form);
//            }
//        }
//        if (kafkaDetail.size()>0) {
//            List<FieldChangeKafka.KafkaDetail> kafkaDetailByIds = 
//                    FieldChangeKafka.getKafkaDetailByIds(kafkaDetail.keySet().toArray(new Long[0]));
//            for (FieldChangeKafka.KafkaDetail kafkaDetailById : kafkaDetailByIds) {
//                for (Map.Entry<Long, OtherChangeTypeDetail> detailEntry : kafkaDetail.entrySet()) {
//                    if(kafkaDetailById.getId().equals(detailEntry.getKey())){
//                        detailEntry.getValue().setChangeDetail(kafkaDetailById); 
//                    }
//                }
//            }
//        }
//        
    }
    
    @Override
    protected String getFormTable() {
        return "release_change_request";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "change_request";
    }
    
    @Override
    public String getFormDisplay() {
        return "综合变更";
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        SubmitReady("submit_ready",          "待提交"),
        TechReady("tech_ready",              "待技术经理审批"),
        TechApproved("tech_approved",        "技术经理通过"),
        TechRejected("tech_rejected",        "技术经理拒绝"),
        DbaApproval("dba_appoval",           "待 DBA 审批"),
        DbaApproved("dba_approved",          "DBA 审批通过"),
        DbaRejected("dba_approved",          "DBA 审批拒绝"),
        Stage02Ready("stage02_ready",        "等待部署集成"),
        Stage02Failure("stage02_failure",     "集成部署失败"),
        Stage02Success("stage02_success",     "集成部署成功"),
        Cancelled("cancelled",               "已经撤销"),
        ReleaseReady("release_ready",        "等待生产发布"),
        ReleaseSuccess("release_success",    "生产部署成功"),
        ReleaseFailure("release_failure",    "生产部署失败"),
        ReleaseRollbacked("release_rollbacked", "生产部署回滚");
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ChangeRequestFormSimple>(
                "默认查询", ChangeRequestFormSimple.class,
                ChangeRequestQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        CkOtherCreate(EventCkOtherCreate.class) ,
        CkOtherEdit(EventCkOtherEdit.class),
        Submit(EventChangeSubmit.class),
        TechReject(EventTechReject.class),
        TechApprove(EventTechApprove.class),
        Cancel(EventChangeCanceled.class) ,
        MarkDeploySuccess(EventMarkDeploySuccess.class),
        MarkDeployFailure(EventMarkDeployFailure.class),
        TechReadyCreate(EventTechReadyCreate.class),
        TechReadyClose(EventTechReadyClose.class);
        
        private final Class<? extends AbstractStateAction<ChangeRequestFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<ChangeRequestFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    @Setter
    @ToString
    private class ReleaseChangeCreationPrepare extends AbstractStatePrepare {
        
        private final String changeType;
        private OptionReleaseId releaseId;
        
        private ReleaseChangeCreationPrepare(@NonNull ChangeType changeType) {
            this.changeType = changeType.getCode();
        }
    }

    @Getter
    @Setter
    @ToString
    private class ReleaseChangeMqCreationPrepare extends AbstractStatePrepare {

        private String changeType;
        
        private OptionDynamicStandard category;

        private OptionReleaseId releaseId;
        
    }
    
    public class ChangeRequestOwnerChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) throws Exception {
            Long createdBy;
            return form != null && (createdBy = ((ChangeRequestFormSimple) form).getCreatedBy()) != null
                    && SessionContext.getUserId().equals(createdBy);
        }
    }
    
    public static class ChangeTypeCKOtherRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object form) throws Exception {
            return !(form != null
                    && ChangeType.CheckListOther.getCode().equals(((ChangeRequestFormSimple) form).getChangeType()));
        }
    }
    
    /**
     * 获取允许研发自行设置部署状态部署项分类清单
     */
    private static String[] getItemCategoriesDevOnly() {
        
        return CommonUtil.split(
            ContextUtil.getConfigTrimed("system.deploy.item.categories.devonly"),
            "[,;\\s+]",
            CommonUtil.STR_TRIMED | CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE);
    }
    
    /**
     * 检查当前用户是否可以设置该部署项的状态
     *
     */
    public class ChangeRequestOwnerDeployChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) throws Exception {
            String[] devCategories;
            ChangeRequestFormSimple originForm = (ChangeRequestFormSimple)form;
            return originForm != null && (devCategories = getItemCategoriesDevOnly()) != null
                    && ArrayUtils.contains(devCategories, originForm.getCategory())
                    && SessionContext.getUserId() != null 
                    && SessionContext.getUserId().equals(originForm.getCreatedBy());
        }
        
    }
    
    public class ChangeReleaseSubsystemsParser implements AuthorityScopeIdMultipleParser {
        
        @Override
        public String[] getAuthorityScopeIds(Object form) throws Exception {
            String category;
            OptionReleaseId releaseId;
            if (form == null || !(form instanceof ChangeRequestFormSimple)
                    || (releaseId = ((ChangeRequestFormSimple) form).getReleaseId()) == null
                    || StringUtils.isBlank(category = releaseId.getCategory())) {
                return null;
            }
            SubsystemFormSimple subsystem;
            if ((subsystem = SubsystemService.getInstance().getByCode(SubsystemFormSimple.class, category)) == null) {
                return null;
            }
            return SubsystemBasicUtil.subsytemIdToBusinessId(new long[] { subsystem.getId() });
        }
    }
    
    /**
     * 暂时只检测上线日期格式正确，且在当天（-8h考虑可能在凌晨发布的场景）的日期之后
     */
    private boolean releaseIdEnsureOpened(OptionReleaseId releaseId) {
        if (releaseId == null) {
            return false;
        }
        String releaseDate;
        if ((releaseDate = releaseId.getReleaseDate()) != null && releaseDate.matches("^\\d{4}\\-\\d{2}\\-\\d{2}$")) {
            return false;
        }
        
        return Long.valueOf("releaseDate".replace("-", "")) >= Long
                .valueOf(DateFormatUtils.format(System.currentTimeMillis() - MilliSecondsOf8Hours, "yyyyMMdd"));
    }
    
    /**
     * 确认上线项目是分配给当前用户的
     */
    private OptionReleaseId getOpenedReleaseIdAssignedToMe(String releaseId) throws Exception {
        List<? extends OptionReleaseId> options;
        if ((options = FieldChangeRequestReleaseId.queryDynamicValues(new String[] { releaseId }, true)) == null
                || options.isEmpty()) {
            throw new MessageException(String.format("上线项目（%s）不存在，或未分配给你", releaseId));
        }
        if (!releaseIdEnsureOpened(options.get(0))) {
            throw new MessageException(String.format("上线项目（%s）已经过期, 不再被允许使用", releaseId));
        }
        return options.get(0);
    }
    
    public class EventCkOtherCreate extends AbstractStateCreateAction<ChangeRequestFormSimple, ChangeRequestFormOtherCreation> {
        
        public EventCkOtherCreate() {
            super(ChangeType.CheckListOther.getDisplay(), STATES.SubmitReady.getCode());
        }

        @Override
        public boolean flowMatched(ChangeRequestFormSimple originForm) {
            return ChangeType.CheckListOther.getCode().equals(originForm.getChangeType());
        }
        
        @Override
        public boolean prepareRequired() {
            return true;
        }
        
        @Override
        public ReleaseChangeCreationPrepare prepare(String event, ChangeRequestFormSimple originForm) throws Exception {
            ReleaseChangeCreationPrepare newChange = new ReleaseChangeCreationPrepare(ChangeType.CheckListOther);
            for (NameValuePair nameValuePair : getContextFormEventParams()) {
                if ("releaseId".equals(nameValuePair.getName())) {
                    newChange.setReleaseId(getOpenedReleaseIdAssignedToMe(nameValuePair.getValue()));
                    break;
                }
            }
            return newChange;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, ChangeRequestFormSimple originForm, final ChangeRequestFormOtherCreation form,
                String message) throws Exception {
            final ChangeType changeType = ChangeType.CheckListOther;
            ensureChangeTypeCategoryExisted(changeType, form.getCategory().getOptionValue());
            OptionReleaseId releaseId = getOpenedReleaseIdAssignedToMe(form.getReleaseId().getOptionValue());
            Long role = getFormBaseDao().queryAsObject(Long.class, "SELECT u.role FROM `release_change_requirement_user` u where release_id = ? and username = ?"
                    , new Object[]{releaseId, SessionContext.getUsername()});
            if (role == null || role != 0) {
                throw new MessageException("上线项目不存在或当前用户不是研发参与人");
            }

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(),
                    new ObjectMap().put("title", form.getTitle())
                            .put("change_type", changeType.getCode())
                            .put("release_id", releaseId.getReleaseId())
                            .put("description", form.getDescription())
                            .put("scope_type", form.getScopeType())
                            .put("category", form.getCategory().getOptionValue())
                            .put("created_at", new Date())
                            .put("created_by", SessionContext.getTokenUserId())),
                    new AbstractDao.ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            id.set(r.getLong(1));
                        }
                    });
            handleAttachments(form.getAttachements(), id.get());
            handleApplications(form.getApplications(), id.get());
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventCkOtherEdit extends AbstractStateAction<ChangeRequestFormSimple, ChangeRequestFormOtherCreation, Void> {
        
        public EventCkOtherEdit() {
            super("编辑", getStateCodes(STATES.SubmitReady , STATES.TechRejected, STATES.Cancelled), "");
        }

        @Override
        public boolean flowMatched(ChangeRequestFormSimple originForm) {
            return ChangeType.CheckListOther.getCode().equals(originForm.getChangeType());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ChangeRequestOwnerChecker.class, rejecter = ChangeTypeCKOtherRejecter.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {

        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, ChangeRequestFormOtherCreation form,
                String message) throws Exception {
            final ChangeType changeType = ChangeType.CheckListOther;
            ensureChangeTypeCategoryExisted(changeType, form.getCategory().getOptionValue());
            OptionReleaseId releaseId = getOpenedReleaseIdAssignedToMe(form.getReleaseId().getOptionValue());
            getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                    .put("=id", originForm.getId())
                    .put("title", form.getTitle())
                    .put("change_type", changeType.getCode())
                    .put("release_id", releaseId.getReleaseId())
                    .put("description", form.getDescription())
                    .put("scope_type", form.getScopeType())
                    .put("category", form.getCategory().getOptionValue())
                ));
            handleAttachments(form.getAttachements(), form.getId());
            handleApplications(form.getApplications(), form.getId());
            return null ;
        }
    }
    
    private void ensureChangeTypeCategoryExisted(ChangeType changeType, String category) {
        Category categoryEnum = Category.get(category);
        if (categoryEnum == null || !categoryEnum.containsChangeType(changeType)) {
            throw new MessageException("变更单的分类不存在，或不被支持");
        }
    }
    
    private void genChangeDeployItemName(long id) throws Exception {
        ChangeRequestFormDetail form;
        if ((form = getForm(id)) != null) {
            getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                    .put("=id", form.getId())
                    .put("deploy_item_name", form.genDeployItemName())
                ));
        }
    }
    
    public class EventChangeSubmit extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventChangeSubmit() {
            super("提交", getStateCodes(STATES.SubmitReady, STATES.TechRejected, STATES.Cancelled),
                    STATES.TechReady.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ChangeRequestOwnerChecker.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            genChangeDeployItemName(form.getId());
            return null;
        }
    }
    
    private final AbstractStateChoice choiceDeployProduction = new AbstractStateChoice(
            "是否需要部署生产？",
            STATES.ReleaseReady.getCode(),
            STATES.Stage02Success.getCode()) {
        @Override
        protected boolean select(AbstractStateFormInput form) {
            return ((ChangeRequestFormSimple)getContextFormOrigin()).productionNeedToDeploy();
        }
        
        @Override
        public boolean flowMatched(AbstractStateFormBase originForm, boolean yesOrNo) {
           return yesOrNo == select(originForm);
        }
    };
    
    /**
     * 针对撤销操作，目前未针对类型进行拆分，但在很多场景下
     * 不同类型可能需要做一些变更回滚的操作，或者是权限控制
     * 此时，就必须通过类型自行实现检查以及授权的定义
     */
    public class EventChangeCanceled extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventChangeCanceled() {
            super("撤销", getStateCodes(
                        STATES.SubmitReady, 
                        STATES.TechReady,
                        STATES.TechApproved,
                        STATES.TechRejected,
                        STATES.Stage02Failure,
                        STATES.ReleaseReady
            ), STATES.Cancelled.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ChangeRequestOwnerChecker.class )
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventMarkDeploySuccess extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventMarkDeploySuccess() {
            super("标记部署成功", getStateCodes(STATES.Stage02Failure, STATES.Stage02Ready), choiceDeployProduction);
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ChangeRequestOwnerDeployChecker.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventMarkDeployFailure extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventMarkDeployFailure() {
            super("标记部署失败", getStateCodes(STATES.Stage02Ready, STATES.ReleaseReady), STATES.Stage02Failure.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = ChangeRequestOwnerDeployChecker.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            return null;
        }
    }
    
    private final AbstractStateChoice choiceDeployStage02 = new AbstractStateChoice(
        "是否部署集成？", STATES.Stage02Ready.getCode(), STATES.ReleaseReady.getCode()) {
        @Override
        protected boolean select(AbstractStateFormInput form) {
            return ((ChangeRequestFormSimple)getContextFormOrigin()).stage02NeedToDeploy();
        }
        
        @Override
        public boolean flowMatched(AbstractStateFormBase originForm, boolean yesOrNo) {
           if (ScopeType.get(((ChangeRequestFormSimple)originForm).getScopeType()).isNeedDeployIntegration()) {
               return yesOrNo;
           }
           return !yesOrNo;
        }
    };
    
    private final AbstractStateChoice choiceApprovalNeedDBA = new AbstractStateChoice(
                    "是否需要DBA审批？", STATES.DbaApproval.getCode(), choiceDeployStage02) {
        @Override
        protected boolean select(AbstractStateFormInput form) throws Exception {
            return false;
        }
        
        @Override
        public boolean flowMatched(AbstractStateFormBase originForm, boolean yesOrNo) {
            if (ChangeType.get(((ChangeRequestFormSimple)originForm).getChangeType()).isNeedDbaApproval()) {
                return yesOrNo;
            }
            return !yesOrNo;
        }
    };
    
    public class EventTechApprove extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventTechApprove() {
            super("技术审批通过", STATES.TechReady.getCode(), choiceApprovalNeedDBA);
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, multipleParser = ChangeReleaseSubsystemsParser.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventTechReject extends AbstractStateAction<ChangeRequestFormSimple, StateFormBasicInput, Void> {
        
        public EventTechReject() {
            super("技术审批拒绝", STATES.TechReady.getCode(), STATES.TechRejected.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Business, multipleParser = ChangeReleaseSubsystemsParser.class)
        public void check(String event, ChangeRequestFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ChangeRequestFormSimple originForm, StateFormBasicInput form, String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventTechReadyCreate extends AbstractStateTodoCreateAction<ChangeRequestFormSimple> {
        
        public EventTechReadyCreate() {
            super("创建待技术经理审批待办事项", STATES.TechReady.getCode());
        }
        
        @Override
        protected String getTodoTitle(String event, ChangeRequestFormSimple originForm, AbstractStateFormInput form)
                throws Exception {
            return String.format("变更申请：%s - %s - %s", originForm.getReleaseId().getReleaseId(), "技术经理审批",
                    originForm.getTitle());
        }
        
        @Override
        protected long[] getTodoAssignees(String event, ChangeRequestFormSimple originForm, AbstractStateFormInput form)
                throws Exception {
            return ConvertUtil
                    .asNonNullUniquePrimitiveLongArray(getActionUserIds(EVENTS.TechApprove.getName(), originForm));
        }
    }
    
    public class EventTechReadyClose extends AbstractStateTodoCloseAction<ChangeRequestFormSimple> {
        
        public EventTechReadyClose() {
            super("关闭待技术经理审批待办事项", STATES.TechReady.getCode());
        }
        
        @Override
        protected String getClosedTodoEvent(String event, ChangeRequestFormSimple originForm, AbstractStateFormInput form)
                throws Exception {
            return EVENTS.TechReadyCreate.getName();
        }
    }
    
    private void handleApplications(OptionApplication[] applications, long id) throws Exception {
        if (applications == null) {
            return;
        }
        List<ObjectMap> sqlValues = new LinkedList<>();
        for (OptionApplication item : applications) {
            if (item == null) {
                continue;
            }
            sqlValues.add(new ObjectMap().put("change_request_id", id).put("application_id", item.getId()));
        }
        
        getFormBaseDao().executeUpdate("DELETE FROM release_change_application WHERE change_request_id = ?",
                new Object[] { id });
        if (sqlValues.size() <= 0) {
            return;
        }
        getFormBaseDao().executeUpdate(SqlQueryUtil.pairs2InsertQuery("release_change_application", sqlValues));
    }
    
    private void handleAttachments(CommonFormAttachement[] attachments, long id) throws Exception {
        if (attachments == null) {
            return;
        }
        Set<Long> attachementIds = new HashSet<>();
        for (CommonFormAttachement item : attachments) {
            if (item == null) {
                continue;
            }
            attachementIds.add(item.getId());
        }
        AttachmentService.cleanByTargetFormField(getFormName(), id, "attachments");
        AttachmentService.bindWithForm(getFormName(), id, attachementIds.toArray(new Long[0]));
    }
    
    @Override
    public <T extends ChangeRequestFormSimple> T getForm(Class<T> clazz, long id) throws Exception {
        PagedList<T> paged = listForm(
                clazz,
                new ChangeRequestQueryDefault(1, 1)
                        .setId(id)
        );
        List<T> changes;
        if (paged == null || (changes = paged.getList()) == null || changes.size() <= 0) {
            return null;
        }
        return changes.get(0);
    }
}
