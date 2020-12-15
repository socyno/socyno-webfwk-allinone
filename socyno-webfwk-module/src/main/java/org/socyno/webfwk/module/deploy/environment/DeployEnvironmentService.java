package org.socyno.webfwk.module.deploy.environment;

import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.annotation.Authority;
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

public class DeployEnvironmentService extends
        AbstractStateFormServiceWithBaseDao<DeployEnvironmentFormDetail, DeployEnvironmentFormDetail, DeployEnvironmentFormSimple> {
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    protected String getFormTable() {
        return "system_deploy_environment";
    }
    
    @Override
    public String getFormName() {
        return "system_deploy_environment";
    }
    
    @Override
    public String getFormDisplay() {
        return "应用部署环境";
    }
    
    @Getter
    private static final DeployEnvironmentService Instance = new DeployEnvironmentService();
    
    private DeployEnvironmentService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
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
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<DeployEnvironmentFormDetail>(
            "默认查询",
            DeployEnvironmentFormDetail.class,
            DeployEnvironmentQueryDefault.class
        ));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate
            extends AbstractStateSubmitAction<DeployEnvironmentFormDetail, DeployEnvironmentFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DeployEnvironmentFormDetail form, String sourceState) {
        }
        
        @Override
        public Long handle(String event, DeployEnvironmentFormDetail originForm,
                final DeployEnvironmentFormCreation form, String message) throws Exception {
            
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao()
                    .executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(),
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
    }
    
    public class EventEdit
            extends AbstractStateAction<DeployEnvironmentFormDetail, DeployEnvironmentFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
        }
        
        @Override
        public Void handle(String event, DeployEnvironmentFormDetail originForm, DeployEnvironmentFormEdition form,
                String message) throws Exception {
            
            getFormBaseDao()
                    .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                            new ObjectMap().put("=id", form.getId()).put("name", form.getName())
                                    .put("display", form.getDisplay())
                    ));
            
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<DeployEnvironmentFormDetail, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
            
        }
    }
    
    public class EventDisable extends AbstractStateAction<DeployEnvironmentFormDetail, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", STATES.ENABLED.getCode(), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployEnvironmentFormDetail arg1, String arg2) {
            
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Edit(EventEdit.class),
        Enable(EventEnable.class),
        Disable(EventDisable.class);
        
        private final Class<? extends AbstractStateAction<DeployEnvironmentFormDetail, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<DeployEnvironmentFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
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
        String sql = String.format(SQL_QUERY_ALL, getFormTable());
        if (!disableIncluded) {
            sql = String.format("%s WHERE state_form_status != '%s'", sql, STATES.DISABLED.getCode());
        }
        return queryFormWithStateRevision(clazz, sql);
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

    @Override
    protected void fillExtraFormFields(Collection<? extends DeployEnvironmentFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
