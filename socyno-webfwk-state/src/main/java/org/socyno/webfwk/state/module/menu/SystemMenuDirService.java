package org.socyno.webfwk.state.module.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateDeleteAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.util.StateFormBasicInput;
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
public class SystemMenuDirService extends
    AbstractStateFormServiceWithBaseDao<SystemMenuDirFormDetail, SystemMenuDirFormDefault, SystemMenuDirFormSimple> {
    
    private SystemMenuDirService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemMenuDirService Instance = new SystemMenuDirService();
    
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
        DEFAULT(new StateFormNamedQuery<SystemMenuDirFormDefault>(
            "默认查询", 
            SystemMenuDirFormDefault.class, SystemMenuDirQueryDefault.class
        ))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuDirFormDetail, SystemMenuDirFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemMenuDirFormDetail originForm,
                SystemMenuDirFormCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(),
                            new ObjectMap()
                                    .put("path", form.getPath())
                                    .put("name", form.getName())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("pane_id", form.getPaneId())
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
    
    public class EventUpdate extends AbstractStateAction<SystemMenuDirFormDetail, SystemMenuDirFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuDirFormDetail originForm, final SystemMenuDirFormEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id", form.getId())
                        .put("name", form.getName())
                        .put("path", form.getPath())
                        .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                        .put("order", form.getOrder())
                        .put("pane_id", form.getPaneId())
                    ));
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemMenuDirFormDetail> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuDirFormDetail originForm, final StateFormBasicInput form,
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
        
        private final Class<? extends AbstractStateAction<SystemMenuDirFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemMenuDirFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_menu_dir";
    }
    
    @Override
    protected String getFormTable() {
        return "system_menu_dir";
    }
    
    @Override
    public String getFormDisplay() {
        return "菜单目录";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    protected String loadFormSqlTmpl() {
        return SystemMenuDirQueryDefault.SQL_QUERY_ALL.concat(" AND d.#(formIdField)=#(formIdValue)");
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuDirFormSimple> forms) throws Exception {
        
    }
}
