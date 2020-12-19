package org.socyno.webfwk.module.release.mobapp;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateCreateAction;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class ReleaseMobileAppService extends
        AbstractStateFormServiceWithBaseDao<ReleaseMobileAppFormDetail, ReleaseMobileAppFormDefault, ReleaseMobileAppFormSimple> {
    
    private ReleaseMobileAppService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final ReleaseMobileAppService Instance = new ReleaseMobileAppService();
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "启用"),
        DISABLED("disabled", "禁用");
        
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
         * 启用
         */
        Enabled(EventEnabled.class),
        
        /**
         * 禁用
         */
        Disabled(EventDisabled.class);
        
        private final Class<? extends AbstractStateAction<ReleaseMobileAppFormSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<ReleaseMobileAppFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ReleaseMobileAppFormDefault>("默认查询", ReleaseMobileAppFormDefault.class,
                ReleaseMobileAppQueryDefault.class));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends ReleaseMobileAppFormSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
    }
    
    @Override
    protected String getFormTable() {
        return "release_app_config";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "release_app_configs";
    }
    
    @Override
    public String getFormDisplay() {
        return "应用配置";
    }
    
    public static class UserChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null && SessionContext.getUserId().equals(((ReleaseMobileAppFormSimple) form).getCreatedBy());
        }
        
    }
    
    public class EventCreate extends AbstractStateCreateAction<ReleaseMobileAppFormSimple, ReleaseMobileAppFormCreate> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileAppFormSimple form, String message) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, ReleaseMobileAppFormSimple originForm, ReleaseMobileAppFormCreate form,
                String message) throws Exception {
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getUsername())
                            .put("created_name_by", SessionContext.getDisplay())
                            .put("application_name", form.getApplicationName())
                            .put("store_type", form.getStoreType())
                            .put("approver", form.getApprover().getOptionValue())
            ), new AbstractDao.ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    resultSet.next();
                    id.set(resultSet.getLong(1));
                }
            });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<ReleaseMobileAppFormSimple, ReleaseMobileAppFormUpdate, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileAppFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileAppFormSimple originForm, ReleaseMobileAppFormUpdate form,
                String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("application_name", form.getApplicationName())
                            .put("store_type", form.getStoreType())
                            .put("approver", form.getApprover().getOptionValue())
            ));
            return null;
        }
    }
    
    public class EventEnabled extends AbstractStateAction<ReleaseMobileAppFormSimple, StateFormBasicForm, Void> {
        
        public EventEnabled() {
            super("启用", getStateCodes(STATES.DISABLED), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileAppFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileAppFormSimple originForm, StateFormBasicForm form, String sourceState)
                throws Exception {
            return null;
        }
    }
    
    public class EventDisabled extends AbstractStateAction<ReleaseMobileAppFormSimple, StateFormBasicForm, Void> {
        
        public EventDisabled() {
            super("禁用", getStateCodes(STATES.ENABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileAppFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileAppFormSimple originForm, StateFormBasicForm form, String sourceState)
                throws Exception {
            return null;
        }
    }
}
