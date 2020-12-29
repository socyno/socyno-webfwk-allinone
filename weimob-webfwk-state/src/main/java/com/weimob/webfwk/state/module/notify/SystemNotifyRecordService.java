package com.weimob.webfwk.state.module.notify;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateCreateAction;
import com.weimob.webfwk.state.abs.AbstractStateEnterAction;
import com.weimob.webfwk.state.abs.AbstractStateFormInput;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialChecker;
import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormSimple.MessageType;
import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormSimple.SendResult;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.state.util.*;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.context.RunableWithSessionContext;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.service.AbstractWeimobNotifyService;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;
import com.weimob.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.ConvertUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Slf4j
public class SystemNotifyRecordService extends
        AbstractStateFormServiceWithBaseDao<SystemNotifyRecordFormDefault, SystemNotifyRecordFormDefault, SystemNotifyRecordFormSimple> {
    
    private SystemNotifyRecordService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private final static SystemNotifyRecordService Instance = new SystemNotifyRecordService();
    
    private final static AbstractWeimobNotifyService EMAIL_SERVICE = new AbstractWeimobNotifyService() {
        
        @Override
        protected String getAppKey() {
            return ContextUtil.getConfigTrimed("system.notify.mail.app.key");
        }
        
        @Override
        protected String getTemplateId() {
            return ContextUtil.getConfigTrimed("system.notify.mail.tmpl.id");
        }
        
        @Override
        public String getRemoteUrl() {
            return ContextUtil.getConfigTrimed("system.notify.alert.apiurl");
        }
        
    };
    
    private final static AbstractWeimobNotifyService WEIXIN_SERVICE = new AbstractWeimobNotifyService() {
        
        @Override
        protected String getAppKey() {
            return ContextUtil.getConfigTrimed("system.notify.weixin.app.key");
        }
        
        @Override
        protected String getTemplateId() {
            return ContextUtil.getConfigTrimed("system.notify.weixin.tmpl.id");
        }
        
        @Override
        public String getRemoteUrl() {
            return ContextUtil.getConfigTrimed("system.notify.alert.apiurl");
        }
        
    };
    
    private final static AbstractWeimobNotifyService MOBILE_SERVICE = new AbstractWeimobNotifyService() {
        
        @Override
        protected String getAppKey() {
            return ContextUtil.getConfigTrimed("system.notify.mobile.app.key");
        }
        
        @Override
        protected String getTemplateId() {
            return ContextUtil.getConfigTrimed("system.notify.mobile.tmpl.id");
        }
        
        @Override
        public String getRemoteUrl() {
            return ContextUtil.getConfigTrimed("system.notify.alert.apiurl");
        }
        
    };
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        CREATED    ("created",     "待发送"),
        CANCELLED  ("cancelled",   "取消发送"),
        FINISHED    ("finished",   "发送完成");
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemNotifyRecordFormSimple, SystemNotifyRecordFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyRecordFormSimple form, String sourceState) {
            
        }
        
        /**
         * 允许创建事件的并发执行
         */
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemNotifyRecordFormSimple originForm, SystemNotifyRecordFormCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("type",            form.getType())
                        .put("content",         form.getContent())
                        .put("message_to",      form.getMessageTo())
                        .put("message_cc",      form.getMessageCc())
            ), new ResultSetProcessor () {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventEnterCreated extends AbstractStateEnterAction<SystemNotifyRecordFormSimple> {
        
        public EventEnterCreated() {
            super("创建自动触发", STATES.CREATED.getCode());
        }
        
        @Override
        public void check(String event, SystemNotifyRecordFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemNotifyRecordFormSimple originForm, AbstractStateFormInput form, String message) throws Exception {
            if (form == null || form.getId() == null) {
                return null;
            }
            new Thread(new RunableWithSessionContext() {
                @Override
                public void exec() {
                    try {
                        Thread.sleep(5000);
                        SystemNotifyRecordFormSimple originForm = getForm(form.getId());
                        StateFormBasicInput triggerForm = new StateFormBasicInput();
                        triggerForm.setId(originForm.getId());
                        triggerForm.setRevision(originForm.getRevision());
                        triggerAction(EVENTS.SendNow.getName(), triggerForm);
                    } catch(Exception e) {
                        log.error("Auto send trigger failure", e);
                    }
                }
            }).start();
            return null;
        }
        
    }
    
    public class EventEdit extends AbstractStateAction<SystemNotifyRecordFormSimple, SystemNotifyRecordFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return false;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyRecordFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemNotifyRecordFormSimple originForm, final SystemNotifyRecordFormEdition form, final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                getFormTable(), new ObjectMap()
                        .put("=id",             form.getId())
                        .put("type",            form.getType())
                        .put("content",         form.getContent())
                        .put("message_to",      form.getMessageTo())
                        .put("message_cc",      form.getMessageCc())
                    ));
            return null;
        }
    }
    
    public class EventCancel extends AbstractStateAction<SystemNotifyRecordFormSimple, StateFormBasicInput, Void> {
        
        public EventCancel() {
            super("取消", STATES.CREATED.getCode(), STATES.CANCELLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyRecordFormSimple form, String sourceState) {
            
        }
    }
    
    public class EventResend extends AbstractStateAction<SystemNotifyRecordFormSimple, StateFormBasicInput, Void> {

        public EventResend() {
            super("重发", getStateCodesEx(STATES.CREATED), STATES.CREATED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyRecordFormSimple form, String sourceState) {
        
        }
    }
    
    public class IsOwnerChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) throws Exception {
            return ((SystemNotifyRecordFormSimple) form).getCreatedBy().equals(SessionContext.getTokenUserId());
        }
    }
    
    public class EventSendNow extends AbstractStateAction<SystemNotifyRecordFormSimple, StateFormBasicInput, StateFormEventResultMessageView> {

        public EventSendNow() {
            super("立即发送", STATES.CREATED.getCode(), STATES.FINISHED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = IsOwnerChecker.class)
        public void check(String event, SystemNotifyRecordFormSimple form, String sourceState) {
            
        }
        
        private String[] parseMultipleContents(String body) {
            return CommonUtil.split(body, "\\s+\\-{10,}\\s*MULTIPLE-BODY\\s*\\-{10,}\\s+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
        }
        
        private String[] parseMultipleReceivers(String receivers) {
            return CommonUtil.split(receivers, "[,;]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
        }
        
        @Override
        public StateFormEventResultMessageView handle(String event, SystemNotifyRecordFormSimple originForm, StateFormBasicInput form, String message) {
            try {
                String body;
                if ((body = StringUtils.trimToEmpty(originForm.getContent())).isEmpty()) {
                    throw new MessageException("No Body");
                }
                if (StringUtils.isNotBlank(parseMailContentKeyword(body, "sendSkiped", "sendSkipped"))) {
                    setNotifySendResult(form.getId(), SendResult.Skipped);
                    return new StateFormEventResultMessageView("忽略发送");
                }
                final AtomicInteger successCount = new AtomicInteger(0);
                final AtomicInteger failureCount = new AtomicInteger(0);
                if (MessageType.Email.getValue().equalsIgnoreCase(originForm.getType())) {
                    for (String singleBody : parseMultipleContents(body)) {
                        String[] receivers = parseMultipleReceivers(
                                String.format("%s,%s", StringUtils.trimToEmpty(originForm.getMessageTo()),
                                        StringUtils.trimToEmpty(parseMailContentKeyword(singleBody, "receivers",
                                                "recipients", "addressesTo", "copyperson", "addressesCc"))));
                        if (EMAIL_SERVICE.send(singleBody, receivers, AbstractWeimobNotifyService.OPTION_NOEXCEPTOIN_ONERROR)) {
                            successCount.addAndGet(1);
                        } else {
                            failureCount.addAndGet(1);
                        }
                        Thread.sleep(1000);
                    }
                } else if (MessageType.Weixin.getValue().equalsIgnoreCase(originForm.getType())) {
                    for (String singleBody : parseMultipleContents(body)) {
                        String[] receivers = parseMultipleReceivers(
                                String.format("%s,%s", StringUtils.trimToEmpty(originForm.getMessageTo()),
                                        StringUtils.trimToEmpty(parseMailContentKeyword(singleBody, "receivers",
                                                "recipients", "addressesTo"))));
                        if (WEIXIN_SERVICE.send(singleBody, receivers, AbstractWeimobNotifyService.OPTION_NOEXCEPTOIN_ONERROR)) {
                            successCount.addAndGet(1);
                        } else {
                            failureCount.addAndGet(1);
                        }
                        Thread.sleep(1000);
                    }
                } else if (MessageType.Message.getValue().equalsIgnoreCase(originForm.getType())) {
                    for (String singleBody : parseMultipleContents(body)) {
                        String[] receivers = parseMultipleReceivers(
                                String.format("%s,%s", StringUtils.trimToEmpty(originForm.getMessageTo()),
                                        StringUtils.trimToEmpty(parseMailContentKeyword(singleBody, "receivers",
                                                "recipients", "addressesTo"))));
                        if (MOBILE_SERVICE.send(singleBody, receivers, AbstractWeimobNotifyService.OPTION_NOEXCEPTOIN_ONERROR)) {
                            successCount.addAndGet(1);
                        } else {
                            failureCount.addAndGet(1);
                        }
                        Thread.sleep(1000);
                    }
                } else {
                    throw new MessageException("Unimplemented Message Type");
                }
                SendResult result = failureCount.get() <= 0 ? SendResult.Success
                        : (successCount.get() <= 0 ? SendResult.Failure : SendResult.Partial);
                setNotifySendResult(form.getId(), result);
                return new StateFormEventResultMessageView(String.format("发送结果：%s", result.getDisplay()));
            } catch (Throwable e) {
                try {
                    setNotifySendResult(form.getId(), SendResult.Failure);
                } catch (Exception x) {
                    throw new RuntimeException(x);
                }
                log.error(e.toString(), e);
                return new StateFormEventResultMessageView(String.format("发送结果：%s", SendResult.Failure.getDisplay()))
                        .setEventAppendMessage(CommonUtil.stringifyStackTrace(e));
            }
        }
    }
    
    protected void setNotifySendResult(long id, SendResult result) throws Exception {
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                new ObjectMap().put("=id", id).put("result", result == null ? null : result.getValue())));
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        
        /* 内部事件，当状态进入 created 将自动触发发送事件 */
        EnterCreated(EventEnterCreated.class),
        
        Edit(EventEdit.class),
        
        Cancel(EventCancel.class),
        
        Resend(EventResend.class),
        
        SendNow(EventSendNow.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemNotifyRecordFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemNotifyRecordFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemNotifyRecordFormDefault>("默认查询", 
                SystemNotifyRecordFormDefault.class, SystemNotifyRecordQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_notify_record";
    }
    
    @Override
    public String getFormTable() {
        return "system_notify_record";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统通知记录";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemNotifyRecordFormSimple> forms) throws Exception {
        
    }
    
    private String parseMailContentKeyword(String content, String... targets) {
        if (targets == null || (targets = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) targets)) == null
                || targets.length <= 0) {
            return "";
        }
        for (int i = 0; i < targets.length; i++) {
            targets[i] = CommonUtil.escapeRegexp(targets[i]);
        }
        List<String> address = new ArrayList<String>();
        Pattern pattern = Pattern.compile(
                String.format("<!--\\s*(%s)\\s+([^\\>]+)\\s*-->", StringUtils.join(targets, '|')),
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            address.add(matcher.group(2).trim());
        }
        return StringUtils.join(address, ',');
    }
}
