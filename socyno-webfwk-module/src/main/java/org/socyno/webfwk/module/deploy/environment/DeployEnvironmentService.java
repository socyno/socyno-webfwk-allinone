package org.socyno.webfwk.module.deploy.environment;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class DeployEnvironmentService extends AbstractStateFormServiceWithBaseDao<DeployEnvironmentFormDetail> {

    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }

    protected static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }

    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    protected String getFormTable() {
        return "system_deploy_environment";
    }

    @Override
    protected Map<String, AbstractStateAction<DeployEnvironmentFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<DeployEnvironmentFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    @Override
    public String getFormName() {
        return "system_deploy_environment";
    }

    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }

    public static final DeployEnvironmentService DEFAULT = new DeployEnvironmentService();
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "启用"),
        DISABLED("disabled", "禁用");
        
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
        DEFAULT(new StateFormNamedQuery<DeployEnvironmentFormDetail>("default", DeployEnvironmentFormDetail.class,
                DeployEnvironmentFormDefaultQuery.class));
        
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
        Create(new AbstractStateSubmitAction<DeployEnvironmentFormDetail, DeployEnvironmentFormForCreation>("添加",
                STATES.DISABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, DeployEnvironmentFormDetail form, String sourceState) {
            }
            
            @Override
            public Long handle(String event, DeployEnvironmentFormDetail originForm,
                    final DeployEnvironmentFormForCreation form, String message) throws Exception {
                
                AtomicLong id = new AtomicLong(0);
                DEFAULT.getFormBaseDao()
                        .executeUpdate(SqlQueryUtil.prepareInsertQuery(DEFAULT.getFormTable(),
                                new ObjectMap().put("name", form.getName())
                                        .put("display", form.getDisplay())
                                        .put("created_by", SessionContext.getUserId())
                                        .put("created_code_by", SessionContext.getUsername())
                                        .put("created_name_by", SessionContext.getDisplay())
                                        .put("created_at", new Date())
                                ),
                                new ResultSetProcessor() {
                                    @Override
                                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                                        resultSet.next();
                                        id.set(resultSet.getLong(1));
                                    }
                                });
                return id.get();
            }
        }),
        Edit(new AbstractStateAction<DeployEnvironmentFormDetail, DeployEnvironmentFormForEdition, Void>("编辑",
                STATES.stringifyEx(), (String) null) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
            }
            
            @Override
            public Void handle(String event, DeployEnvironmentFormDetail originForm,
                    DeployEnvironmentFormForEdition form, String message) throws Exception {
                
                DEFAULT.getFormBaseDao()
                        .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                new ObjectMap().put("=id", form.getId()).put("name", form.getName())
                                        .put("display", form.getDisplay())
                        ));
                
                return null;
            }
        }),
        Enable(new AbstractStateAction<DeployEnvironmentFormDetail, BasicStateForm, Void>("启用", 
                STATES.DISABLED.getCode(), STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
                
            }
            
        }),
        Disable(new AbstractStateAction<DeployEnvironmentFormDetail, BasicStateForm, Void>("禁用",
                STATES.ENABLED.getCode(), STATES.DISABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
                
            }
        });
        
        private final AbstractStateAction<DeployEnvironmentFormDetail, ?, ?> action;

        EVENTS(AbstractStateAction<DeployEnvironmentFormDetail, ?, ?> action) {
            this.action = action;
        }

        public AbstractStateAction<DeployEnvironmentFormDetail, ?, ?> getAction() {
            return action;
        }

    }
    
    /**
     * SELECT
     *     e.*
     * FROM
     *     %s e
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    public <T extends AbstractDeployEnvironmentForm> List<T> queryAll(@NonNull Class<T> clazz, boolean disableIncluded) throws Exception {
        String sql = String.format(SQL_QUERY_ALL, DEFAULT.getFormTable());
        if (!disableIncluded) {
            sql = String.format("%s WHERE state_form_status != '%s'", sql, STATES.DISABLED.getCode());
        }
        return DEFAULT.queryFormWithStateRevision(clazz, sql);
    }
    
    public <T extends AbstractDeployEnvironmentForm> List<T> allEnabled(@NonNull Class<T> clazz) throws Exception {
        return queryAll(clazz, false);
    }
    
    public <T extends AbstractDeployEnvironmentForm> List<T> queryByNames(@NonNull Class<T> clazz, boolean disableIncluded, String... names) throws Exception {
        if (names == null || names.length <= 0) {
            return Collections.emptyList();
        }
        
        List<T> environments = new ArrayList<>();
        for (T env : queryAll(clazz, disableIncluded)) {
            if (StringUtils.containsIgnoreCase(names, env.getName())) {
                environments.add(env);
            }
        }
        return environments;
    }
    
    public <T extends AbstractDeployEnvironmentForm> List<T> queryByNamesEnabled(@NonNull Class<T> clazz, String... names) throws Exception {
        return queryByNames(clazz, false, names);
    }
}
