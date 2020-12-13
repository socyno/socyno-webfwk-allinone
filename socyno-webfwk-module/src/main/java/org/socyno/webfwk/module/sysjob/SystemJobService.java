package org.socyno.webfwk.module.sysjob;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.CronExpression;
import org.socyno.webfwk.executor.abs.AbstractJobManager;
import org.socyno.webfwk.executor.abs.AbstractJobStatus;
import org.socyno.webfwk.executor.abs.AbstractStatusCallbackCreater;
import org.socyno.webfwk.executor.model.JobBasicStatus;
import org.socyno.webfwk.executor.model.JobStatusWebsocketLink;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.module.tenant.SystemTenantBasicService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserFormWithSecurity;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.*;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SystemJobService
        extends AbstractStateFormServiceWithBaseDao<SystemJobFormDetail, SystemJobFormDetail, SystemJobFormDetail> {
    @Getter
    private static final SystemJobService Instance = new SystemJobService();
    
    public SystemJobService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    protected String getFormTable() {
        return "external_async_job";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "external_async_job";
    }
    
    @Override
    public String getFormDisplay() {
        return "异步任务";
    }
        
    public class SuperAdminProxyRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object scopeSource) throws Exception {
            return !(SystemTenantBasicService.inSuperTenantProxyContext() && SessionContext.isAdmin());
        }
        
    }
    
    protected AbstractJobManager<?, ?> getJobManager(SystemJobFormDetail originForm) throws Exception {
        Class<?> serviceClass = null;
        try {
            serviceClass = ClassUtil.loadClass(originForm.getServiceClass());
        } catch(Exception e) {
            log.error(e.toString(), e);
        }
        if (serviceClass == null || !AbstractJobManager.class.isAssignableFrom(serviceClass)) {
            throw new MessageException("注册的异步任务服务类不可用");
        }
        Object jobMannagerInstance = serviceClass.getMethod("getInstance", String.class).invoke(null,
                originForm.getServiceInstance());
        if (jobMannagerInstance == null || jobMannagerInstance.getClass() != serviceClass) {
            throw new MessageException("注册的异步任务服务实例不可用");
        }
        return (AbstractJobManager<?, ?>) jobMannagerInstance;
    }
    
    public static CronExpression checkCronExpression(String cronExpression) {
        if (StringUtils.isNotBlank(cronExpression)) {
            try {
                return new CronExpression(cronExpression);
            } catch (Exception e) {
                throw new MessageException("执行计划表达式格式错误", e);
            }
        }
        return null;
    }
    
    private String parserParamsForm(String formDefinition, String defaultParams){

        if (StringUtils.isBlank(formDefinition)) {
            return defaultParams;
        }
        
        final String clzzzpathPrefix = "classpath:";
        if (StringUtils.startsWithIgnoreCase(formDefinition, clzzzpathPrefix)) {
            try {
                formDefinition = formDefinition.substring(clzzzpathPrefix.length());
                formDefinition = ClassUtil.classToJson(ClassUtil.loadClass(formDefinition.trim())).toString();
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }
        try {
            CommonUtil.fromJson(formDefinition, JsonObject.class);
        } catch (Exception e) {
            return defaultParams;
        }
        return formDefinition;
    }
    
    public class EventCreate extends AbstractStateSubmitAction<SystemJobFormDetail, SystemJobFormCreate> {
        
        public EventCreate() {
            super("创建", STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = SuperAdminProxyRejecter.class)
        public void check(String event, SystemJobFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Long handle(String event, SystemJobFormDetail originForm, SystemJobFormCreate form, final String message)
                throws Exception {
            checkCronExpression(form.getCronExpression());
            if (StringUtils.isNotBlank(form.getServiceParametersForm())
                    && StringUtils.isBlank(parserParamsForm(form.getServiceParametersForm(), ""))) {
                throw new MessageException("参数模型输入内容错误");
            }
            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeTransaction(new AbstractDao.ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        getFormTable(), new ObjectMap()
                                .put("title", form.getTitle())
                                .put("service_class", form.getServiceClass())
                                .put("service_instance", form.getServiceInstance())
                                .put("cron_expression", form.getCronExpression())
                                .put("service_parameters_form", form.getServiceParametersForm())
                                .put("description", form.getDescription())
                                .put("created_at", new Date())
                                .put("created_by", SessionContext.getUserId())
                                .put("created_code_by", SessionContext.getUsername())
                                .put("created_name_by", SessionContext.getDisplay())
                                .put("running_tasks", 0)
                                .put("concurrent_allowed", form.getConcurrentAllowed())
                                .put("default_params", StringUtils.isBlank(form.getDefaultParams()) ? "" :
                                        form.getDefaultParams())
                        ),
                        new AbstractDao.ResultSetProcessor() {
                            
                            @Override
                            public void process(ResultSet resultSet, Connection connection) throws Exception {
                                resultSet.next();
                                id.set(resultSet.getLong(1));
                            }
                        });
                }
            });
            
            return id.get();
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemJobFormDetail, SystemJobFormEdit, Void> {
        
        public EventEdit() {
            super("编辑",getStateCodesEx(STATES.RUNNING), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = SuperAdminProxyRejecter.class)
        public void check(String event, SystemJobFormDetail form, String sourceState) {
            
        }

        @Override
        public Void handle(String event, SystemJobFormDetail originForm, SystemJobFormEdit form,
                final String message) throws Exception {
            checkCronExpression(form.getCronExpression());
            getFormBaseDao().executeTransaction(new AbstractDao.ResultSetProcessor() {
                
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                            new ObjectMap().put("=id", originForm.getId())
                                    .put("title", form.getTitle())
                                    .put("service_class", form.getServiceClass())
                                    .put("service_instance", form.getServiceInstance())
                                    .put("concurrent_allowed", form.getConcurrentAllowed())
                                    .put("cron_expression", form.getCronExpression())
                                    .put("service_parameters_form", form.getServiceParametersForm())
                                    .put("default_params", StringUtils.isBlank(form.getDefaultParams()) ? "" :
                                            form.getDefaultParams())
                                    .put("description", form.getDescription())));
                }
            });
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<SystemJobFormDetail, BasicStateForm, Void> {
        
        public EventEnable() {
            super("置空闲", getStateCodesEx(STATES.ENABLED), STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemJobFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemJobFormDetail originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SystemJobFormDetail, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemJobFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemJobFormDetail originForm, BasicStateForm form, final String message)
                throws Exception {
            return null;
        }
    }
    
    public class EventResetRunnings extends AbstractStateAction<SystemJobFormDetail, BasicStateForm, Void> {
        
        public EventResetRunnings() {
            super("重置运行任务数", getStateCodesEx(STATES.RUNNING), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemJobFormDetail form, String sourceState) {
            
        }

        @Override
        public Void handle(String event, SystemJobFormDetail originForm, BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
                            .put("running_tasks", 0)));
            return null;
        }
    }
    
    public class EventExecute
            extends AbstractStateAction<SystemJobFormDetail, DynamicStateForm, StateFormEventResultWebSocketViewLink> {
        
        public EventExecute() {
            super("运行", getStateCodes(STATES.ENABLED), STATES.RUNNING.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String events, SystemJobFormDetail form, String sourceState) {
            
        }
        
        @Override
        public String getDynamicFormDefinition(String event, SystemJobFormDetail originForm) throws Exception {
            String definition = super.getDynamicFormDefinition(event, originForm);
            return parserParamsForm(originForm.getServiceParametersForm(),definition);
        }
        
        @Override
        public SystemJobResultView handle(String event,
                final SystemJobFormDetail originForm, final DynamicStateForm form, final String message)
                throws Exception {
            int maxConcurrentAllowed = CommonUtil.parseMinimalInteger(originForm.getConcurrentAllowed(), 100);
            if (CommonUtil.ifNull(originForm.getRunningTasks(), 0) >= maxConcurrentAllowed) {
                throw new MessageException(String.format("当前运行的任务数已达到最大允许并发数（%s）", maxConcurrentAllowed));
            }
            
            if (StringUtils.isNotBlank(originForm.getDefaultParams())) {
                JsonObject defaultJsonData = CommonUtil.fromJson(originForm.getDefaultParams(), JsonObject.class);
                JsonObject jsonData = (JsonObject) form.getJsonData();
                if (jsonData == null) {
                    form.setJsonData(defaultJsonData);
                } else {
                    for (Map.Entry<String, JsonElement> e : defaultJsonData.entrySet()) {
                        if(jsonData.has(e.getKey()) && 
                                (jsonData.get(e.getKey()) == null || JsonNull.INSTANCE.equals(jsonData.get(e.getKey())))){
                            ((JsonObject)form.getJsonData()).add(e.getKey(),e.getValue());
                            continue;
                        }
                        if (!jsonData.has(e.getKey())) {
                            ((JsonObject)form.getJsonData()).add(e.getKey(),e.getValue());
                        }
                    }
                }
            }
            AbstractJobManager<?, ?> jobManager = getJobManager(originForm);
            long taskId = jobManager.trigger(HttpMessageConverter.toInstance(jobManager.getParametersTypeClass(),
                    CommonUtil.ifNull(form.getJsonData(), "{}")), new AbstractStatusCallbackCreater() {
                        
                        @Override
                        public void fetched(AbstractJobStatus status) {
                            
                        }
                        
                        /**
                         * 当执行完成后，将运行数减一，同时视情况更新状态
                         */
                        @Override
                        public void completed(AbstractJobStatus status, Throwable exception) {
                            try {
                                getFormBaseDao()
                                        .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                                                new ObjectMap().put("=id", originForm.getId()).put("#running_tasks",
                                                        "running_tasks - 1")));
                                saveStateRevision(originForm.getId(), STATES.ENABLED.getCode(),
                                        new ObjectMap().put("<=running_tasks", 0), STATES.RUNNING.getCode());
                            } catch (Exception e) {
                                log.error(e.toString(), e);
                            }
                            
                            JobBasicStatus jobManager = null;
                            if(status instanceof JobBasicStatus){
                                jobManager = (JobBasicStatus)status;
                            }

                            List<SystemUserFormWithSecurity> usersSecurity = null;
                            try {
                                usersSecurity = SystemUserService.DEFAULT.getUsersSecurity(SessionContext.getUserId());
                            } catch (Exception e) {
                                log.error(e.toString(),e);
                            }

                            SystemNotifyService.sendAsync(
                                    "system.external.async.job.result.notify",
                                    new ObjectMap()
                                            .put("originForm", originForm)
                                            .put("status", status)
                                            .put("exception", exception)
                                            .put("notifyInfo", usersSecurity)
                                            .put("jobManager", jobManager)
                                            .asMap(), 0);
                        }
                    });
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap().put("=id", originForm.getId())
                    .put("#running_tasks", "running_tasks + 1")));
            JobStatusWebsocketLink link = jobManager.getStatusWebSocketLink(taskId);
            return new SystemJobResultView(taskId, link.getWebsocketUrl(), link.getParameters());
        }
    }
    
    public class EventJobStatus
            extends AbstractStateAction<SystemJobFormDetail, SystemJobFormStatus, AbstractJobStatus> {
        
        public EventJobStatus() {
            super("查看状态", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String events, SystemJobFormDetail form, String sourceState) {
            
        }
        
        @Override
        public AbstractJobStatus handle(String event,
                final SystemJobFormDetail originForm, final SystemJobFormStatus form, final String message)
                throws Exception {
            return getJobManager(originForm).status(form.getJobId());
        }
        
        @Override
        public Boolean messageRequired() {
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        /**
         * 异步通常为系统管理用途，其数据存放于租户数据库，即不同租户有所区别。
         * 但这些任务的创建和编辑属于租户管理的范畴，不允许租户的用户进行操作，
         * 为此，只允许以超级管理员代理的用户进行创建和编辑。
         */
        Create(EventCreate.class),
        /**
         * 异步通常为系统管理用途，其数据存放于租户数据库，即不同租户有所区别。
         * 但这些任务的创建和编辑属于租户管理的范畴，不允许租户的用户进行操作，
         * 为此，只允许以超级管理员代理的用户进行创建和编辑。
         */
        Edit(EventEdit.class),
        
        Enable(EventEnable.class),
        
        Disable(EventDisable.class),
        
        ResetRunnings(EventResetRunnings.class),
        
        Execute(EventExecute.class),
        
        JobStatus(EventJobStatus.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemJobFormDetail, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemJobFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled","空闲"),
        RUNNING("runing" ,"运行中"),
        DISABLED("disabled","禁用");
        
        private final String code;
        private final String name;

        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemJobFormDetail>("default",
                SystemJobFormDetail.class, SystemJobQueryDefault.class));

        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemJobFormDetail> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
