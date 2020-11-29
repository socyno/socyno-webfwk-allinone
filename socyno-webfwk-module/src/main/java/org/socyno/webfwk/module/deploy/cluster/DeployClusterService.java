package org.socyno.webfwk.module.deploy.cluster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

import lombok.Getter;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

public class DeployClusterService extends AbstractStateFormServiceWithBaseDao<DeployClusterFormDetail> {

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
        return "system_deploy_cluster";
    }

    @Override
    protected Map<String, AbstractStateAction<DeployClusterFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<DeployClusterFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }

    @Override
    public String getFormName() {
        return "system_deploy_cluster";
    }

    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }

    public static final DeployClusterService DEFAULT = new DeployClusterService();

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<DeployClusterFormDefaultForm>("default",
                DeployClusterFormDefaultForm.class, DeployClusterFormDefaultQuery.class));

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
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Submit(new AbstractStateSubmitAction<DeployClusterFormDetail, DeployClusterFormForCreation>("添加",
                STATES.DISABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, DeployClusterFormDetail form, String sourceState) {
            }
            
            @Override
            public Long handle(String event, DeployClusterFormDetail originForm,
                    final DeployClusterFormForCreation form, String message) throws Exception {

                AtomicLong id = new AtomicLong(0);
                getDao().executeUpdate(
                        SqlQueryUtil.prepareInsertQuery(
                                DEFAULT.getFormTable(),
                                new ObjectMap().put("code", form.getCode())
                                        .put("title", form.getTitle())
                                        .put("type", form.getType())
                                        .put("environment", form.getEnvironment())
                                        .put("api_service", form.getApiService())
                                        .put("api_client_cert", form.getApiClientCert())
                                        .put("api_client_token", form.getApiClientToken())
                                        .put("description", form.getDescription())
                                        .put("created_by", SessionContext.getUserId())
                                        .put("created_code_by", SessionContext.getUsername())
                                        .put("created_name_by", SessionContext.getDisplay())
                                        .put("created_at", new Date())), new ResultSetProcessor() {
                            @Override
                            public void process(ResultSet resultSet, Connection connection) throws Exception {
                                resultSet.next();
                                id.set(resultSet.getLong(1));
                            }
                        });
                return id.get();
            }
        }),
        Edit(new AbstractStateAction<DeployClusterFormDetail, DeployClusterFormForEdit, Void>("编辑",
                STATES.stringifyEx(), (String) null) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
            }

            @Override
            public Void handle(String event, DeployClusterFormDetail originForm, DeployClusterFormForEdit form,
                    String message) throws Exception {

                getDao().executeUpdate(
                        SqlQueryUtil.prepareUpdateQuery(
                                DEFAULT.getFormTable(),
                                new ObjectMap().put("=id", form.getId())
                                        .put("code", form.getCode())
                                        .put("title", form.getTitle())
                                        .put("type",  form.getType())
                                        .put("environment", form.getEnvironment())
                                        .put("api_service", form.getApiService())
                                        .put("api_client_cert", form.getApiClientCert())
                                        .put("api_client_token", form.getApiClientToken())
                                        .put("description", form.getDescription())));

                return null;
            }
        }),
        Enable(new AbstractStateAction<DeployClusterFormDetail, BasicStateForm, Void>("启用", 
                STATES.DISABLED.getCode(), STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
                
            }
            
        }),
        Disable(new AbstractStateAction<DeployClusterFormDetail, BasicStateForm, Void>("禁用",
                STATES.ENABLED.getCode(), STATES.DISABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
                
            }
            
        });
        
        private final AbstractStateAction<DeployClusterFormDetail, ?, ?> action;

        EVENTS(AbstractStateAction<DeployClusterFormDetail, ?, ?> action) {
            this.action = action;
        }

        public AbstractStateAction<DeployClusterFormDetail, ?, ?> getAction() {
            return action;
        }
    }
}
