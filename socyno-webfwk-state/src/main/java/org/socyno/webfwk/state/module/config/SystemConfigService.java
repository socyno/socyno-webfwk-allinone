package org.socyno.webfwk.state.module.config;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateDeleteAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class SystemConfigService extends
        AbstractStateFormServiceWithBaseDao<SystemConfigFormDetail, SystemConfigFormDefault, SystemConfigFormSimple> {
    
    @Getter
    private static final SystemConfigService Instance = new SystemConfigService();
    
    private SystemConfigService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    public static final String FORM_DISPLAY = "系统参数配置";

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
         * 禁用
         */
        Delete(EventDelete.class);
        
        private final Class<? extends AbstractStateAction<SystemConfigFormSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemConfigFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemConfigFormDefault>("默认查询",
                SystemConfigFormDefault.class, SystemConfigQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemConfigFormSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
    }
    
    @Override
    protected String getFormTable() {
        return "system_configs";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    public String getFormName() {
        return "system_configs";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统参数";
    }
    
    public class UserChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((SystemConfigFormSimple) form).getCreatedBy());
        }

    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemConfigFormSimple, SystemConfigFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemConfigFormSimple form, String message) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemConfigFormSimple originForm,
                SystemConfigFormCreation form, String message) throws Exception {
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("name", form.getName())
                            .put("value", form.getValue())
                            .put("comment", form.getComment())
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
    
    public class EventEdit extends AbstractStateAction<SystemConfigFormSimple, SystemConfigFormUpdate, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemConfigFormSimple originForm, String message) {
            
        }
        
        @Override
        public Void handle(String event, SystemConfigFormSimple originForm, SystemConfigFormUpdate form, String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("name", form.getName())
                            .put("value", form.getValue())
                            .put("comment", form.getComment())
            ));
            return null;
        }
    }

    public class EventDelete extends AbstractStateDeleteAction<SystemConfigFormSimple> {
        
        public EventDelete() {
            super("删除", getStateCodes(STATES.ENABLED));
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemConfigFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemConfigFormSimple originForm, StateFormBasicInput form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            return null;
        }
    }
}
