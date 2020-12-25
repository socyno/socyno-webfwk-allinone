package com.weimob.webfwk.module.release.mobapp;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateCreateAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialChecker;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.state.sugger.DefaultStateFormSugger;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.state.util.StateFormEventClassEnum;
import com.weimob.webfwk.state.util.StateFormEventResultCreateViewBasic;
import com.weimob.webfwk.state.util.StateFormNamedQuery;
import com.weimob.webfwk.state.util.StateFormQueryBaseEnum;
import com.weimob.webfwk.state.util.StateFormStateBaseEnum;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;

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
    
    public class EventEnabled extends AbstractStateAction<ReleaseMobileAppFormSimple, StateFormBasicInput, Void> {
        
        public EventEnabled() {
            super("启用", getStateCodes(STATES.DISABLED), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileAppFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileAppFormSimple originForm, StateFormBasicInput form, String sourceState)
                throws Exception {
            return null;
        }
    }
    
    public class EventDisabled extends AbstractStateAction<ReleaseMobileAppFormSimple, StateFormBasicInput, Void> {
        
        public EventDisabled() {
            super("禁用", getStateCodes(STATES.ENABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileAppFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ReleaseMobileAppFormSimple originForm, StateFormBasicInput form, String sourceState)
                throws Exception {
            return null;
        }
    }
}
