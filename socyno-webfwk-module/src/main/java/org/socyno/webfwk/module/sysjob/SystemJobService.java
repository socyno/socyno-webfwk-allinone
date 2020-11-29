package org.socyno.webfwk.module.sysjob;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
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
import org.socyno.webfwk.state.module.user.SystemUserSecurityOnly;
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
public class SystemJobService extends AbstractStateFormServiceWithBaseDao<SystemJobDetailForm> {
    
    public static final SystemJobService DEFAULT = new SystemJobService();
    
    @Override
    protected String getFormTable() {
        return "external_async_job";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    public String getFormName() {
        return "external_async_job";
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemJobDetailForm, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemJobDetailForm, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    public static class SuperAdminProxyRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object scopeSource) throws Exception {
            return !(SystemTenantBasicService.inSuperTenantProxyContext() && SessionContext.isAdmin());
        }
        
    }
    
    protected static AbstractJobManager<?, ?> getJobManager(SystemJobDetailForm originForm) throws Exception {
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
    
    protected static CronExpression checkCronExpression(String cronExpression) {
        if (StringUtils.isNotBlank(cronExpression)) {
            try {
                return new CronExpression(cronExpression);
            } catch (Exception e) {
                throw new MessageException("执行计划表达式格式错误", e);
            }
        }
        return null;
    }
    
    private static String parserParamsForm(String formDefinition, String defaultParams){

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
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        /**
         * 异步通常为系统管理用途，其数据存放于租户数据库，即不同租户有所区别。
         * 但这些任务的创建和编辑属于租户管理的范畴，不允许租户的用户进行操作，
         * 为此，只允许以超级管理员代理的用户进行创建和编辑。
         */
        Create(new AbstractStateSubmitAction<SystemJobDetailForm, SystemJobCreateForm>("创建",
                STATES.DISABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System, rejecter = SuperAdminProxyRejecter.class)
            public void check(String event, SystemJobDetailForm form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, SystemJobDetailForm originForm, SystemJobCreateForm form, final String message)
                    throws Exception {
                checkCronExpression(form.getCronExpression());
                if (StringUtils.isNotBlank(form.getServiceParametersForm())
                        && StringUtils.isBlank(parserParamsForm(form.getServiceParametersForm(), ""))) {
                    throw new MessageException("参数模型输入内容错误");
                }
                AtomicLong id = new AtomicLong(0);
                DEFAULT.getFormBaseDao().executeTransaction(new AbstractDao.ResultSetProcessor() {
                    @Override
                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            DEFAULT.getFormTable(), new ObjectMap()
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
        }),
        /**
         * 异步通常为系统管理用途，其数据存放于租户数据库，即不同租户有所区别。
         * 但这些任务的创建和编辑属于租户管理的范畴，不允许租户的用户进行操作，
         * 为此，只允许以超级管理员代理的用户进行创建和编辑。
         */
        Edit(new AbstractStateAction<SystemJobDetailForm, SystemJobEditForm, Void>("编辑",
                STATES.stringifyEx(STATES.RUNNING), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System, rejecter = SuperAdminProxyRejecter.class)
            public void check(String event, SystemJobDetailForm form, String sourceState) {
                
            }

            @Override
            public Void handle(String event, SystemJobDetailForm originForm, SystemJobEditForm form,
                    final String message) throws Exception {
                checkCronExpression(form.getCronExpression());
                DEFAULT.getFormBaseDao().executeTransaction(new AbstractDao.ResultSetProcessor() {
                    
                    @Override
                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
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
        }),
        
        Enable(new AbstractStateAction<SystemJobDetailForm, BasicStateForm, Void>("置空闲",
                STATES.stringifyEx(STATES.ENABLED), STATES.ENABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemJobDetailForm form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, SystemJobDetailForm originForm, BasicStateForm form,
                    final String message) throws Exception {
                return null;
            }
        }),
        
        Disable(new AbstractStateAction<SystemJobDetailForm, BasicStateForm, Void>("禁用",
                STATES.stringifyEx(STATES.DISABLED), STATES.DISABLED.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemJobDetailForm form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, SystemJobDetailForm originForm, BasicStateForm form,
                    final String message) throws Exception {
                return null;
            }
        }),
        
        ResetRunningTasks(new AbstractStateAction<SystemJobDetailForm, BasicStateForm, Void>(
                "重置运行任务数", STATES.stringifyEx(STATES.RUNNING), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemJobDetailForm form, String sourceState) {
                
            }

            @Override
            public Void handle(String event, SystemJobDetailForm originForm, BasicStateForm form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                        DEFAULT.getFormTable(), new ObjectMap().put("=id", originForm.getId())
                        .put("running_tasks", 0)));
                return null;
            }
        }),
        
        Execute(new AbstractStateAction<SystemJobDetailForm, DynamicStateForm, StateFormEventResultWebSocketViewLink>(
                "运行", STATES.stringify(STATES.ENABLED), STATES.RUNNING.getCode()) {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String events, SystemJobDetailForm form, String sourceState) {
                
            }
            
            @Override
            public String getDynamicFormDefinition(String event, SystemJobDetailForm originForm) throws Exception {
                String definition = super.getDynamicFormDefinition(event, originForm);
                return parserParamsForm(originForm.getServiceParametersForm(),definition);
            }
            
            @Override
            public SystemJobResultView handle(String event,
                    final SystemJobDetailForm originForm, final DynamicStateForm form, final String message)
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
                                    DEFAULT.getFormBaseDao()
                                            .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                                    new ObjectMap().put("=id", originForm.getId()).put("#running_tasks",
                                                            "running_tasks - 1")));
                                    DEFAULT.saveStateRevision(originForm.getId(), STATES.ENABLED.getCode(),
                                            new ObjectMap().put("<=running_tasks", 0), STATES.RUNNING.getCode());
                                } catch (Exception e) {
                                    log.error(e.toString(), e);
                                }
                                
                                
                                JobBasicStatus jobManager = null;
                                if(status instanceof JobBasicStatus){
                                    jobManager = (JobBasicStatus)status;
                                }

                                List<SystemUserSecurityOnly> usersSecurity = null;
                                try {
                                    usersSecurity = SystemUserService.DEFAULT.getUsersSecurity(SessionContext.getUserId());
                                } catch (Exception e) {
                                    log.error(e.toString(),e);
                                }

                                SystemNotifyService.sendAsync(
                                        "system.external.async.job.result.notify",
                                        new ObjectMap()
                                                .put("notifyService", DEFAULT)
                                                .put("originForm", originForm)
                                                .put("status", status)
                                                .put("exception", exception)
                                                .put("notifyInfo", usersSecurity)
                                                .put("jobManager", jobManager)
                                                .asMap(), 0);
                            }
                        });
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                        DEFAULT.getFormTable(), new ObjectMap().put("=id", originForm.getId())
                        .put("#running_tasks", "running_tasks + 1")));
                JobStatusWebsocketLink link = jobManager.getStatusWebSocketLink(taskId);
                return new SystemJobResultView(taskId, link.getWebsocketUrl(), link.getParameters());
            }
        }),
        
        JobStatus(new AbstractStateAction<SystemJobDetailForm, SystemJobStatusForm, AbstractJobStatus>(
                "查看任务状态", STATES.stringifyEx(), "") {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String events, SystemJobDetailForm form, String sourceState) {
                
            }
            
            @Override
            public AbstractJobStatus handle(String event,
                    final SystemJobDetailForm originForm, final SystemJobStatusForm form, final String message)
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
        })
        ;
        
        private final AbstractStateAction<SystemJobDetailForm, ?, ?> action;
        
        EVENTS(AbstractStateAction<SystemJobDetailForm, ?, ?> action) {
            this.action = action;
        }
        
        @Override
        public AbstractStateAction<SystemJobDetailForm, ?, ?> getAction() {
            return action;
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
        DEFAULT(new StateFormNamedQuery<SystemJobDetailForm>("default",
                SystemJobDetailForm.class, SystemJobDefaultQuery.class));

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
}
