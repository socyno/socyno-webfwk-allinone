package org.socyno.webfwk.state.module.notify;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class SystemNotifyTemplateService extends
        AbstractStateFormServiceWithBaseDao<SystemNotifyTemplateFormDefault, SystemNotifyTemplateFormDefault, SystemNotifyTemplateFormSimple> {
    
    private SystemNotifyTemplateService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private final static SystemNotifyTemplateService Instance = new SystemNotifyTemplateService();
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  ("enabled", "有效") ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    private void checkNotifyTemplateFormChange(SystemNotifyTemplateFormCreation changed) throws Exception {
        if (StringUtils.isBlank(changed.getCode()) || changed.getCode().matches("^\\s*\\d+\\s*$")) {
            throw new MessageException("通知模板的代码，不允许为纯数字！");
        }
        StringBuffer sql = new StringBuffer(
                String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable()));
        if (changed.getId() != null) {
            sql.append("AND id != ").append(changed.getId());
        }
        if (getFormBaseDao().queryAsObject(Long.class, sql.toString(),
                new Object[] { changed.getCode() }) > 0) {
            throw new MessageException(String.format("通知模板的代码名称(%s)已被占用，请重新命名！", changed.getCode()));
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemNotifyTemplateFormSimple, SystemNotifyTemplateFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateFormSimple form, String sourceState) {
            
        }
            
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemNotifyTemplateFormSimple originForm, SystemNotifyTemplateFormCreation form, String message) throws Exception {
            checkNotifyTemplateFormChange(form);
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("code",            form.getCode())
                        .put("comment",         form.getComment())
                        .put("mail_to",         form.getMailTo())
                        .put("mail_cc",         form.getMailCc())
                        .put("mail_content",    form.getMailContent())
                        .put("message_to",      form.getMessageTo())
                        .put("message_content", form.getMessageContent())
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
    
    public class EventEdit extends AbstractStateAction<SystemNotifyTemplateFormSimple, SystemNotifyTemplateFormEdition, Void> {
       
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemNotifyTemplateFormSimple originForm, final SystemNotifyTemplateFormEdition form, final String message) throws Exception {
            checkNotifyTemplateFormChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                getFormTable(), new ObjectMap()
                    .put("=id",            form.getId())
                    .put("code",            form.getCode())
                    .put("comment",         form.getComment())
                    .put("mail_to",         form.getMailTo())
                    .put("mail_cc",         form.getMailCc())
                    .put("mail_content",    form.getMailContent())
                    .put("message_to",      form.getMessageTo())
                    .put("message_content", form.getMessageContent())
                    ));
            return null;
        }
    }
    
    public class EventDisabled extends AbstractStateAction<SystemNotifyTemplateFormSimple, StateFormBasicInput, Void> {
        
        public EventDisabled() {
            super("禁用", getStateCodesEx(), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateFormSimple form, String sourceState) {
            
        }
    }
    
    public class EventEnabled extends AbstractStateAction<SystemNotifyTemplateFormSimple, StateFormBasicInput, Void> {
        
        public EventEnabled() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateFormSimple form, String sourceState) {
            
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建
         */
        Create(EventCreate.class),
        
        /**
         * 更新
         */
        Edit(EventEdit.class),
        /**
         * 禁用
         */
        Disable(EventDisabled.class),
        
        /**
         * 恢复
         */
        Enable(EventEnabled.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemNotifyTemplateFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemNotifyTemplateFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemNotifyTemplateFormDefault>("默认查询", 
                SystemNotifyTemplateFormDefault.class, SystemNotifyTemplateQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public SystemNotifyTemplateFormSimple getByCode(String tmplCode) throws Exception {
        if (StringUtils.isBlank(tmplCode)) {
            return null;
        }
        List <SystemNotifyTemplateFormSimple> list;
        PagedList<SystemNotifyTemplateFormSimple> paged;
        if ((paged = listForm(SystemNotifyTemplateFormSimple.class,
                new SystemNotifyTemplateQueryDefault(1, 1).setCodeEquals(tmplCode)))
                == null || (list = paged.getList()) == null || list.size() <= 0 ) {
            return null;
        }
        return list.get(0);
    }
    
    @Override
    public String getFormName() {
        return "system_notify_template";
    }
    
    @Override
    public String getFormTable() {
        return "system_notify_template";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统通知模板";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemNotifyTemplateFormSimple> forms) throws Exception {
        
    }
}
