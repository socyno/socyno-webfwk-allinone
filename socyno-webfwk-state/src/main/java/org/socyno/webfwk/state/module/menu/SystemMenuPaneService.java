package org.socyno.webfwk.state.module.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateCreateAction;
import org.socyno.webfwk.state.util.StateFormBasicForm;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

import lombok.Getter;

public class SystemMenuPaneService extends
        AbstractStateFormServiceWithBaseDao<SystemMenuPaneFormDetail, SystemMenuPaneFormDefault, SystemMenuPaneFormSimple> {
    
    private SystemMenuPaneService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemMenuPaneService Instance = new SystemMenuPaneService();
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
          ENABLED  ("enabled", "有效")
        , DISABLED ("disabled", "禁用")
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
        DEFAULT(new StateFormNamedQuery<SystemMenuPaneFormDefault>(
            "默认查询", 
            SystemMenuPaneFormDefault.class, SystemMenuPaneQueryDefault.class
        ));
        private StateFormNamedQuery<SystemMenuPaneFormDefault> namedQuery;
        
        QUERIES(StateFormNamedQuery<SystemMenuPaneFormDefault> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuPaneFormDetail, SystemMenuPaneFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemMenuPaneFormDetail originForm,
                SystemMenuPaneFormCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(),
                            new ObjectMap()
                                    .put("path", form.getPath())
                                    .put("name", form.getName())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order", form.getOrder())
                ), new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        r.next();
                        id.set(r.getLong(1));
                    }
                });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemMenuPaneFormDetail, SystemMenuPaneFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuPaneFormDetail originForm, final SystemMenuPaneFormEdition form,
                final String message) throws Exception {
            getFormBaseDao()
                    .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                            new ObjectMap()
                                    .put("=id", form.getId())
                                    .put("name", form.getName())
                                    .put("path", form.getPath())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order", form.getOrder())
                                ));
            return null;
        }
    }
    
    public class EventDelete extends  AbstractStateDeleteAction<SystemMenuPaneFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuPaneFormDetail originForm, final StateFormBasicForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Update(EventUpdate.class),
        Delete(EventDelete.class);
        
        private final Class<? extends AbstractStateAction<SystemMenuPaneFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemMenuPaneFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_menu_pane";
    }
    
    @Override
    protected String getFormTable() {
        return "system_menu_pane";
    }
    
    @Override
    public String getFormDisplay() {
        return "菜单导航";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuPaneFormSimple> forms) throws Exception {
        
    }
}
