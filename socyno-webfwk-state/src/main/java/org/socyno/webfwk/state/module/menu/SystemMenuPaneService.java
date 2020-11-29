package org.socyno.webfwk.state.module.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.Getter;
public class SystemMenuPaneService extends AbstractStateFormServiceWithBaseDao<SystemMenuPaneDetail> {
    
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
        
        public static String[] stringify(STATES... states) {
            if (states == null || states.length <= 0) {
                return new String[0];
            }
            String[] result = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                result[i] = states[i].getCode();
            }
            return result;
        }
        
        public static String[] stringifyEx(STATES... states) {
            if (states == null) {
                states = new STATES[0];
            }
            List<String> result = new ArrayList<>(states.length);
            for (STATES s : STATES.values()) {
                if (!ArrayUtils.contains(states, s)) {
                    result.add(s.getCode());
                }
            }
            return result.toArray(new String[0]);
        }

        public static List<? extends FieldOption> getStatesAsOption() {
            List<FieldOption> options = new ArrayList<>();
            for (STATES s : STATES.values()) {
                options.add(new FieldSimpleOption(s.getCode(), s.getName()));
            }
            return options;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemMenuPaneListDefaultForm>("default", 
                SystemMenuPaneListDefaultForm.class, SystemMenuPaneListDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Create(new AbstractStateSubmitAction<SystemMenuPaneDetail, SystemMenuPaneForCreation>("创建", STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuPaneDetail form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, SystemMenuPaneDetail originForm, SystemMenuPaneForCreation form, String message)
                    throws Exception {
                final AtomicLong id = new AtomicLong(-1);
                DEFAULT.getFormBaseDao().executeUpdate(
                        SqlQueryUtil.prepareInsertQuery(DEFAULT.getFormTable(),
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
                return id.get();
            }
        }),
        Update(new AbstractStateAction<SystemMenuPaneDetail, SystemMenuPaneForEdition, Void>("编辑", STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuPaneDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemMenuPaneDetail originForm, final SystemMenuPaneForEdition form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao()
                        .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                new ObjectMap()
                                        .put("=id", form.getId())
                                        .put("name", form.getName())
                                        .put("path", form.getPath())
                                        .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                        .put("order", form.getOrder())
                                    ));
                return null;
            }
        }),
        Delete(new AbstractStateDeleteAction<SystemMenuPaneDetail>("删除", STATES.stringifyEx()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuPaneDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemMenuPaneDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(DEFAULT.getFormTable(),
                        new ObjectMap().put("=id", originForm.getId())));
                return null;
            }
        });
        
        private final AbstractStateAction<SystemMenuPaneDetail, ?, ?> action;
        
        EVENTS(AbstractStateAction<SystemMenuPaneDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SystemMenuPaneDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    public static final SystemMenuPaneService DEFAULT = new SystemMenuPaneService();
    
    @Override
    public String getFormName() {
        return getName();
    }
    
    @Override
    protected String getFormTable() {
        return getTable();
    }
    
    protected static String getTable() {
        return "system_menu_pane";
    }
    
    protected static String getName() {
        return "system_menu_pane";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemMenuPaneDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemMenuPaneDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    private static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }

    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
}
