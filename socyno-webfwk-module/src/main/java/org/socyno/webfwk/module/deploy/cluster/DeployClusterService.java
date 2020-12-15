package org.socyno.webfwk.module.deploy.cluster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
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

public class DeployClusterService extends AbstractStateFormServiceWithBaseDao<DeployClusterFormDetail, DeployClusterFormDefault, DeployClusterFormSimple> {

    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    protected String getFormTable() {
        return "system_deploy_cluster";
    }

    @Override
    public String getFormName() {
        return "system_deploy_cluster";
    }
    
    @Override
    public String getFormDisplay() {
        return "应用部署集群";
    }
    
    @Getter
    private static final DeployClusterService Instance = new DeployClusterService();
    
    private DeployClusterService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<DeployClusterFormDefault>(
            "默认查询",
            DeployClusterFormDefault.class, 
            DeployClusterQueryDefault.class
        ));

        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

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
    
    public class EventCreate extends AbstractStateSubmitAction<DeployClusterFormDetail, DeployClusterFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DeployClusterFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Long handle(String event, DeployClusterFormDetail originForm,
                final DeployClusterFormCreation form, String message) throws Exception {

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(
                            getFormTable(),
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
    }
    
    public class EventEdit extends AbstractStateAction<DeployClusterFormDetail, DeployClusterFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
        }

        @Override
        public Void handle(String event, DeployClusterFormDetail originForm, DeployClusterFormEdition form,
                String message) throws Exception {

            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(
                            getFormTable(),
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
    }
    
    public class EventEnable extends AbstractStateAction<DeployClusterFormDetail, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
            
        }
    }
    
    public class EventDisable extends AbstractStateAction<DeployClusterFormDetail, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", STATES.ENABLED.getCode(), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String arg0, DeployClusterFormDetail arg1, String arg2) {
            
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Edit(EventEdit.class),
        Enable(EventEnable.class),
        Disable(EventDisable.class);
        
        private final Class<? extends AbstractStateAction<DeployClusterFormDetail, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<DeployClusterFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends DeployClusterFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
