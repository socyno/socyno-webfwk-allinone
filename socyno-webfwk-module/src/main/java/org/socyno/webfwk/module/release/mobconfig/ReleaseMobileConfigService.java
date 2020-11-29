package org.socyno.webfwk.module.release.mobconfig;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class ReleaseMobileConfigService extends AbstractStateFormServiceWithBaseDaoV2<ReleaseMobileConfigSimple> {
    
    public ReleaseMobileConfigService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    public static final String FORM_DISPLAY = "上架应用参数配置";
    
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
        
        private final Class<? extends AbstractStateAction<ReleaseMobileConfigSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<ReleaseMobileConfigSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ReleaseMobileConfigListForm>("默认查询",
                ReleaseMobileConfigListForm.class, ReleaseMobileConfigDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public static final ReleaseMobileConfigService DEFAULT = new ReleaseMobileConfigService();
    
    @Override
    protected void fillExtraFormFields(Collection<? extends ReleaseMobileConfigSimple> forms) throws Exception {
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
    
    public static class UserChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((ReleaseMobileConfigSimple) form).getCreatedBy());
        }
        
    }

    public class EventCreate extends AbstractStateSubmitAction<ReleaseMobileConfigSimple, ReleaseMobileConfigCreation> {

        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileConfigSimple form, String message) {

        }
        
        @Override
        public Long handle(String event, ReleaseMobileConfigSimple originForm, ReleaseMobileConfigCreation form, String message) throws Exception {
            
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
            return id.get();
        }
    }

    public class EventEdit extends AbstractStateAction<ReleaseMobileConfigSimple, ReleaseMobileConfigForUpdate, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileConfigSimple originForm, String message) {

        }
        
        @Override
        public Void handle(String event, ReleaseMobileConfigSimple originForm, ReleaseMobileConfigForUpdate form, String message) throws Exception {
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

    public class EventEnabled extends AbstractStateAction<ReleaseMobileConfigSimple, BasicStateForm, Void> {
        
        public EventEnabled() {
            super("启用", getStateCodes(STATES.DISABLED), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileConfigSimple originForm, String sourceState) {

        }
        
        @Override
        public Void handle(String event, ReleaseMobileConfigSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            return null;
        }
    }

    public class EventDisabled extends AbstractStateAction<ReleaseMobileConfigSimple, BasicStateForm, Void> {
        
        public EventDisabled() {
            super("禁用", getStateCodes(STATES.ENABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileConfigSimple originForm, String sourceState) {

        }
        
        @Override
        public Void handle(String event, ReleaseMobileConfigSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            return null;
        }
    }


    @Override
    public ReleaseMobileConfigDetail getForm(long formId) throws Exception {
        return getForm(ReleaseMobileConfigDetail.class, formId);
    }

}
