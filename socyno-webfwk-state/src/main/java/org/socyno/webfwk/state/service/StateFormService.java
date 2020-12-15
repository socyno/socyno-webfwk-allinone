package org.socyno.webfwk.state.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.http.NameValuePair;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.exec.StateFormCustomFieldFormNotFoundException;
import org.socyno.webfwk.state.field.FilterAbstractFrom;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.model.StateFlowChartDefinition;
import org.socyno.webfwk.state.model.StateFlowChartLinkData;
import org.socyno.webfwk.state.model.StateFlowChartNodeData;
import org.socyno.webfwk.state.model.CommonSimpleLog;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.HttpMessageConverter;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.github.reinert.jjschema.v1.FieldType;
import com.github.reinert.jjschema.v1.FieldType.FieldOptionsType;
import com.google.gson.JsonElement;

@Slf4j
public class StateFormService {

    @Getter
    private static class StateFormInstance {
        private final StateFormRegister form;
        private final Class<?> serviceClass;
        private final AbstractStateFormServiceWithBaseDao<?, ?, ?> serviceInstance;
        
        StateFormInstance(StateFormRegister form, Class<?> serviceClass,
                AbstractStateFormServiceWithBaseDao<?, ?, ?> serviceInstance) {
            this.form = form;
            this.serviceClass = serviceClass;
            this.serviceInstance = serviceInstance;
        }
    }
    
    private static final Map<String, StateFormInstance> STATE_FORM_INSTANCES
                            = new ConcurrentHashMap<>();
    
    private static class FormCustomDataCacher implements Runnable {
        @Override
        public void run() {
            refresh();
        }
        
        @Getter
        @Setter
        @ToString
        public static class FormCustomView {
            private String formName;
            private String formView;
        }
        
        /**
         * SELECT DISTINCT
         *     class_path form_name,
         *     form_attrs form_view
         * FROM
         *     system_form_viewattrs
         */
        @Multiline
        private static final String SQL_QUERY_FORM_VIEW = "x";
        
        public void refresh(String ...forms) {
            try {
                Object[] args = null;
                StringBuilder sql = new StringBuilder(SQL_QUERY_FORM_VIEW);
                if (forms != null && forms.length > 0) {
                    sql.append(" WHERE class_path in ")
                        .append(CommonUtil.join("?", forms.length, ", ", "(", ")"));
                    args = (Object[])forms;
                }
                saveToCache(getDao().queryAsList(FormCustomView.class, sql.toString(), args), 0);
            } catch (Exception e) {
                log.error("Failed to load configs.", e);
            }
        }
        
        private void saveToCache(List<FormCustomView> list, int offset) {
            int listSize;
            if (list == null || (listSize = list.size()) <= 0 || offset >= listSize) {
                return;
            }
            String currentForm = null;
            String currentAttrs = null;
            try {
                while (offset < listSize) {
                    currentForm = (String) list.get(offset).getFormName();
                    currentAttrs = (String) list.get(offset).getFormView();
                    ClassUtil.AttributesProccessor.setCustomFormAttributes(currentForm,
                            ClassUtil.AttributesProccessor.parseFormCustomizedProperties(currentForm, currentAttrs));
                    offset++;
                }
            } catch (Exception ex) {
                log.error(String.format("Failed to load form custom attributes, form = %s", currentForm), ex);
                saveToCache(list, ++offset);
            }
        }
    }
    
    private final static FormCustomDataCacher FORM_DATA_CACHER = new FormCustomDataCacher();
    
    /**
     * 初始化定时器，用于加载流程表单的配置数据
     */
    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(FORM_DATA_CACHER, 20, 30,
                TimeUnit.SECONDS);
    }
    
    /**
     * 初始化定时器，用于移除已经删除或变更的流程表单定义的缓存数据，确保其一段时间后可被重新加载
     */
    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (StateFormInstance fi : STATE_FORM_INSTANCES.values()) {
                    try {
                        String form = fi.getForm().getFormName();
                        Long found = getDao().queryAsObject(Long.class,
                                "SELECT COUNT(1) FROM system_form_defined WHERE form_name = ? AND revision = ?",
                                new Object[] { form, fi.getForm().getRevision() });
                        if (found <= 0) {
                            STATE_FORM_INSTANCES.remove(form);
                        }
                    } catch (Exception e) {
                        log.error("Failed to load form config data", e);
                    }
                }
            }
            
        }, 115, 60, TimeUnit.SECONDS);
    }
    
    /**
     * 初始化定时器，加载表单的相关显示配置数据
     */
    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run () {
                try {
                    StateFormDisplayScheduled.reload();
                }catch (Exception e){
                    log.error("Failed to load form display data", e);
                }
            }
        }, 10, 120, TimeUnit.SECONDS);
    }
    
    public static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Data
    public static class StateFormRegister {
        private Long id;
        private String formName;
        private String formService;
        private String formDisplay;
        private String formBackend;
        private String rowsExpand;
        private Integer disabled;
        private Long revision;
        private String visibleActions;
        
        public boolean isEnabled() {
            return disabled == null || disabled == 0;
        }
    }
    
    @Getter
    @Setter
    @ToString
    public static class CustomFormViewConfig {
        private long revision;
        private String formClass;
        private String formAttrs;
        
        public CustomFormViewConfig() {
            
        }
        
        public CustomFormViewConfig(String formClass, String formAttrs, long revision) {
            this.formClass = formClass;
            this.formAttrs = formAttrs;
            this.revision = revision;
        }
    }
    
    /**
     * 注册新的表单
     * @param register
     * @throws Exception
     */
    public static void registerForm(StateFormRegister register) throws Exception {
        if (register == null || StringUtils.isBlank(register.getFormName())
                || StringUtils.isBlank(register.getFormDisplay())
                || StringUtils.isBlank(register.getFormService())
                || StringUtils.isBlank(register.getFormBackend())) {
            throw new MessageException("通用流程表单定义数据不规范。");
        }
        getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
            "system_form_defined", new ObjectMap()
                    .put("form_name", StringUtils.trimToEmpty(register.getFormName()))
                    .put("form_backend", StringUtils.trimToEmpty(register.getFormBackend()))
                    .put("form_display", StringUtils.trimToEmpty(register.getFormDisplay()))
                    .put("form_service", StringUtils.trimToEmpty(register.getFormService()))
                    .put("visible_actions", StringUtils.trimToEmpty(register.getVisibleActions()))
        ));
    }
    
    /**
     * 更新已注册的表单
     * @param register
     * @throws Exception
     */
    public static void updateForm(StateFormRegister register) throws Exception {
        if (register == null || StringUtils.isBlank(register.getFormName())
                || StringUtils.isBlank(register.getFormDisplay())
                || StringUtils.isBlank(register.getFormService())
                || StringUtils.isBlank(register.getFormBackend())) {
            throw new MessageException("通用流程表单定义数据不规范。");
        }
        getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
            "system_form_defined", new ObjectMap()
                    .put("=form_name", register.getFormName())
                    .put("form_backend", register.getFormBackend().trim())
                    .put("form_display", register.getFormDisplay().trim())
                    .put("form_service", register.getFormService().trim())
                    .put("rows_expand", register.getRowsExpand())
                    .put("disabled", CommonUtil.ifNull(register.getDisabled(), 0) == 0 ? 0 : 1)
                    .put("visible_actions", StringUtils.trimToEmpty(register.getVisibleActions()))
                    .put("#revision", "revision + 1")
        ));
        /* 完成更新后需要清除表单的缓存实例，确保访问时可重新加载 */
        STATE_FORM_INSTANCES.remove(register.getFormName());
    }
    
    /**
     * 删除已注册的表单
     * @param register
     * @throws Exception
     */
    public static void removeForm(String formName) throws Exception {
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet result, Connection conn) throws Exception {
                getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("system_form_defined",
                        new ObjectMap().put("=form_name", formName)));
                getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("system_form_actions",
                        new ObjectMap().put("=form_name", formName)));
            }
        });
        /* 完成更新后需要清除表单的缓存实例 */
        STATE_FORM_INSTANCES.remove(formName);
    }
    
    /**
     * 启动或禁用已注册的表单
     * @param register
     * @throws Exception
     */
    public static void toggleForm(String formName) throws Exception {
        if (getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery("system_form_defined",
                    new ObjectMap().put("disabled", 0).put("=form_name", formName).put("!=disabled", 0)
                )) == 0) {
            getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery("system_form_defined",
                new ObjectMap().put("disabled", 1).put("=form_name", formName).put("=disabled", 0)
            ));
        }
    }
    
    /**
     * 获取注册的表单信息
     */
    public static StateFormRegister getFormRegister(String formName) throws Exception {
        if (StringUtils.isBlank(formName)) {
            return null;
        }
        return getDao().queryAsObject(StateFormRegister.class,
                    "SELECT * FROM system_form_defined WHERE form_name = ?",
                     new Object[ ] {formName});
    }
        
    /**
     * SELECT
     *     f.*
     * FROM
     *     system_form_defined f
     */
    @Multiline
    private final static String SQL_QUERY_DEFINED_FORM = "X";
    
    /**
     * WHERE
     *     f.form_name LIKE CONCAT('%', ? , '%')
     * OR
     *     f.form_display LIKE CONCAT('%', ? , '%')
     */
    @Multiline
    private final static String SQL_QUERY_DEFINED_LIKE = "X";
    
    /**
     * 列举所有已注册的通用表单
     */
    public static List<StateFormRegister> listStateFormRegister() throws Exception {
         return listStateFormRegister(null);
    }
    
    /**
     * 根据给定的关键字检索已注册的通用表单
     */
    public static List<StateFormRegister> listStateFormRegister(String namelike) throws Exception {
        if (StringUtils.isBlank(namelike)) {
            return getDao().queryAsList(StateFormRegister.class, SQL_QUERY_DEFINED_FORM, null);
        }
        return getDao().queryAsList(StateFormRegister.class,
                String.format("%s %s", SQL_QUERY_DEFINED_FORM, SQL_QUERY_DEFINED_LIKE),
                new Object[] { namelike, namelike });
    }
    
    /**
     * 扫描并解析所有已注册的通用表单
     */
    public static void parseStateFormRegister() throws Exception {
        parseStateFormRegister((String)null);
    }
    
    public static void parseStateFormRegister(String backend) throws Exception {
         for (StateFormRegister form : listStateFormRegister()) {
             if (backend != null && backend.equals(form.getFormBackend())) {
                 parseStateFormRegister(form);
             }
         }
    }
    
    @SuppressWarnings("unchecked")
    private static void parseStateFormRegister(StateFormRegister form) throws Exception {
        if (form == null || !form.isEnabled()) {
            log.warn("通用流程单服务({})被禁用, 忽略。", form);
            return;
        }
        Class<?> tmplClass = null;
        try {
            tmplClass = ClassUtil.loadClass(form.getFormService());
        } catch(ClassNotFoundException e) {
            log.warn("通用流程单服务类({})不存在, 忽略。", form);
            return;
        }
        if (!AbstractStateFormServiceWithBaseDao.class.isAssignableFrom(tmplClass)) {
            log.warn("通用流程单服务类({})不符合规范（未继承预定义抽象类）, 忽略。", form);
            return;
        }
        Object tmplObject = null;
        try {
            Method instanceMethod;
            if ((instanceMethod = tmplClass.getMethod("getInstance")) != null
                    && Modifier.isStatic(instanceMethod.getModifiers())
                    && instanceMethod.getReturnType().isAssignableFrom(tmplClass)) {
                tmplObject = instanceMethod.invoke(null);
            }
            if (tmplObject == null) {
                tmplObject = tmplClass.newInstance();
            }
        } catch (NoSuchMethodException e) {
            tmplObject = tmplClass.newInstance();
        } catch (Throwable e) {
            throw new MessageException(
                    String.format("通用流程单服务实例化失败: %s, %s", form.getFormName(), form.getFormService()),
                    e);
        }
        
        AbstractStateFormServiceWithBaseDao<?, ?, ?> serviceObject = (AbstractStateFormServiceWithBaseDao<?, ?, ?>)tmplObject;
        Class<AbstractStateFormServiceWithBaseDao<?, ?, ?>> serviceClass = (Class<AbstractStateFormServiceWithBaseDao<?, ?, ?>>)tmplClass;
        
        if (!StringUtils.equals(form.getFormName(), serviceObject.getFormName())) {
            throw new MessageException(String.format("注册表单名称（%s）与类中定义的名称(%s/%s)不一致.",
                            form.getFormName(), form.getFormService(), serviceObject.getFormName()));
        }
        log.info("开始解析通用流程定义: {}", form);
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet result, Connection conn) throws Exception {
                getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    "system_form_actions", new ObjectMap()
                            .put("=form_name", form.getFormName())
                ));
                /* 首选注册流程定义事件操作 */
                List<ObjectMap> sqlEventsKvPairs = new ArrayList<>(); 
                for (StateFormActionDefinition formAction : serviceObject.getExternalFormActionDefinition()) {
                    Authority authority;
                    if ((authority = formAction.getAuthority()) == null) {
                        throw new MessageException(String.format("通用流程表单事件(%s/%s)未声明授权(Authority)信息",
                                formAction.getFormName(), formAction.getName()));
                    }
                    if (authority.value().checkScopeId() && AuthorityScopeIdNoopParser.class.equals(authority.parser())
                            && AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser())) {
                        throw new MessageException(
                                String.format("通用流程表单事件(%s/%s)授权(Authority)声异常, 必须实现授权标的解析器 parser 或 multipleParser",
                                        formAction.getFormName(), formAction.getName()));
                    }
                    if (!AuthorityScopeIdNoopParser.class.equals(authority.parser())
                            && !AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser())) {
                        throw new MessageException(
                                String.format("通用流程表单事件(%s/%s), 授权标的解析器 parser 和 multipleParser 禁止同时使用",
                                        formAction.getFormName(), formAction.getName()));
                    }
                    if (authority.multipleChoice()
                            && !AuthorityScopeIdNoopMultipleCleaner.class.equals(authority.multipleCleaner())) {
                        throw new MessageException(String.format(
                                "通用流程表单事件(%s/%s), 多授权标的选择器开关(multipleChoice) 和标记清理器(multipleClear) 禁止同时使用",
                                formAction.getFormName(), formAction.getName()));
                    }
                    if (formAction.getEventType() == StateFormActionDefinition.EventType.Submit) {
                        if (authority.multipleChoice()
                                || !AuthorityScopeIdNoopMultipleCleaner.class.equals(authority.multipleCleaner())) {
                            throw new MessageException(String.format(
                                    "通用流程表单事件(%s/%s), 创建事件中禁止使用多授权标的选择器开关(multipleChoice) 和标记清理器(multipleClear",
                                    formAction.getFormName(), formAction.getName()));
                        }
                    }
                    sqlEventsKvPairs.add(new ObjectMap()
                            .put("form_name", form.getFormName())
                            .put("action_name", formAction.getName())
                            .put("form_backend", form.getFormBackend())
                            .put("action_key", AbstractStateFormService.getFormEventKey(form.getFormName(), formAction.getName()))
                            .put("action_display", formAction.getDisplay())
                            .put("action_type", formAction.getEventType())
                            .put("action_form_type", formAction.getEventFormType().name())
                            .put("source_state", StringUtils.join(CommonUtil.ifNull(formAction.getSourceStates(), new String[] {}), ','))
                            .put("target_state", formAction.getTargetState())
                            .put("prepare_required", formAction.isPrepareRequired())
                            .put("message_reuqired", formAction.getMessageRequired())
                            .put("comfirm_reuqired", formAction.isConfirmRequired())
                            .put("revision_ignored", formAction.isStateRevisionChangeIgnored())
                            .put("action_async", formAction.isAsyncEvent())
                            .put("authority_type", authority.value().name())
                            .put("authority_parser", authority.parser().getName())
                            .put("authority_cheker", authority.checker().getName())
                    );
                }
                /* 再插入预定义的非通用事件操作 */
                Map<String, String> extraEvents;
                if ((extraEvents = serviceObject.getFormPredefinedExtraEvents()) != null) {
                    for (Map.Entry<String, String> exEvent : extraEvents.entrySet()) {
                        if (StringUtils.isAnyBlank(exEvent.getKey(), exEvent.getValue())) {
                            continue;
                        }
                        sqlEventsKvPairs.add(new ObjectMap()
                                .put("form_name", form.getFormName())
                                .put("form_backend", form.getFormBackend())
                                .put("action_name", exEvent.getKey())
                                .put("action_key",
                                        AbstractStateFormService.getFormEventKey(form.getFormName(), exEvent.getKey()))
                                .put("action_display", exEvent.getValue())
                                .put("action_type", "NONE")
                                .put("authority_type", AuthorityScopeType.System.name())
                                .put("action_form_type", "")
                                .put("source_state", "")
                                .put("target_state", "")
                                .put("prepare_required", 0)
                                .put("message_reuqired", 1)
                                .put("comfirm_reuqired", 0)
                                .put("revision_ignored", 0)
                                .put("action_async", 0)
                                .put("authority_parser", "")
                                .put("authority_cheker", "")
                        );
                    }
                }
                getDao().executeUpdate(SqlQueryUtil.pairs2InsertQuery("system_form_actions", sqlEventsKvPairs));
            }
        });
        log.info("完成解析通用流程定义: {}", form);
        STATE_FORM_INSTANCES.put(form.getFormName(), new StateFormInstance(form, serviceClass, serviceObject));
    }
    
    private static StateFormInstance getStateFormInstance(String formName) throws Exception {
        if (!STATE_FORM_INSTANCES.containsKey(formName)) {
            parseStateFormRegister(getFormRegister(formName));
        }
        StateFormInstance instance;
        if ((instance = STATE_FORM_INSTANCES.get(formName)) == null) {
            throw new MessageException(String.format("通用流程表单(%s)不存在，或注册信息错误。", formName));
        }
        return instance;
    }
    
    /**
     * 检查表单是否注册
     */
    public static boolean checkFormDefined(String formName) throws Exception {
        return getFormRegister(formName) != null;
    }
    
    private static void resetActionsVisible(Collection<StateFormActionDefinition> actions, StateFormInstance instance) {
        if (actions != null && actions.size() > 0) {
            String[] visibles = CommonUtil.split(instance.getForm().getVisibleActions(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (visibles != null && visibles.length > 0) {
                boolean foundVisible = !"!".equals(visibles[0]);
                for (StateFormActionDefinition a : actions) {
                    a.setVisible(!foundVisible);
                    for (String v: visibles) {
                        if ((v.startsWith("^") && StringUtils.startsWith(a.getName(), v.substring(1)))
                                || (v.endsWith("$") && StringUtils.endsWith(a.getName(), v.substring(0, v.length() - 1)))
                                || v.equals(a.getName())) {
                            a.setVisible(foundVisible);
                        }
                    }
                }
            }
        }
    }
    
    private static void removeHiddenActions(Map<String, String> actions, StateFormInstance instance) {
        if (actions != null && actions.size() > 0) {
            String[] visibles = CommonUtil.split(instance.getForm().getVisibleActions(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (visibles != null && visibles.length > 0) {
                boolean foundVisible = !"!".equals(visibles[0]);
                for (String a : actions.keySet().toArray(new String[0])) {
                    boolean visible = !foundVisible;
                    for (String v: visibles) {
                        if ((v.startsWith("^") && StringUtils.startsWith(a, v.substring(1)))
                                || (v.endsWith("$") && StringUtils.endsWith(a, v.substring(0, v.length() - 1)))
                                || v.equals(a)) {
                            visible = foundVisible;
                        }
                    }
                    if (!visible) {
                        actions.remove(a);
                    }
                }
            }
        }
    }
    
    /**
     * 获取表单定义
     */
    public static StateFormDefinition getFormDefinition(String formName) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        AbstractStateFormServiceWithBaseDao<?, ?, ?> service = instance.getServiceInstance();
        List<StateFormActionDefinition> actions = service.getExternalFormActionDefinition();
        resetActionsVisible(actions, instance);
        List<String> allownActions = new ArrayList<>();
        for (StateFormActionDefinition action : actions) {
            if (service.checkSubmitAction(action.getName(), null)) {
                allownActions.add(action.getName());
            }
        }
        return new StateFormDefinition()
                    .setActions(actions)
                    .setFormClass(ClassUtil.classToJson(service.getRealFormClass()).toString())
                    .setStates(service.getStates())
                    .setName(instance.getForm().getFormName())
                    .setTitle(instance.getForm().getFormDisplay())
                    .setRowsExpand(instance.getForm().getRowsExpand())
                    .setQueries(service.getFormQueryDefinition())
                    .setAllownActions(allownActions.toArray(new String[0]))
                    .setCommentSupported(service.supportCommentFormAction())
                    ;
    }
    
    /**
     SELECT
         view_name
     FROM 
         system_form_extraviews
     WHERE
         form_name = ?
     */
    @Multiline
    private final static String SQL_QUERY_FORM_EXTRA_VIEWS = "X";
    
    /**
     * 获取额外的关联表单清单
     */
    public static List<String> queryFormExtraViews(String formName) throws Exception {
        if (StringUtils.isBlank(formName)) {
            return Collections.emptyList();
        }
        return getDao().queryAsList(String.class, SQL_QUERY_FORM_EXTRA_VIEWS, new Object[] {formName});
    }
    
    /**
     * 保存额外的关联表单清单
     */
    public static void saveFormExtraViews(final String formName, final List<String> views) throws Exception {
        if (views == null) {
            return;
        }
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet result, Connection conn) throws Exception {
                getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_form_extraviews", new ObjectMap()
                                .put("=form_name", formName)
                    ));
                for (String view : views) {
                    if (StringUtils.isBlank(view)) {
                        continue;
                    }
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        "system_form_extraviews", new ObjectMap()
                                .put("form_name", formName)
                                .put("view_name", view)
                    ));
                }
                
            }});
    }
    
    /**
     * 获取额外的关联表单清单
     */
    public static List<String> queryFormExtraViewDefinitions(String formName) throws Exception {
        List<String> definitions = new ArrayList<>();
        for (String view : queryFormExtraViews(formName)) {
            if (StringUtils.isBlank(view)) {
                continue;
            }
            try {
                definitions.add(ClassUtil.classToJson(ClassUtil.loadClass(view)).toString());
            } catch(Exception e) {
                throw new MessageException(String.format("解析界面视图模型定义(%s)失败！", view), e);
            }
        }
        return definitions;
    }
    
    /**
     * 获取表单事件的请求数据类型
     */
    public static Class<AbstractStateForm> getActionFormTypeClass(String formName, String event) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        return instance.getServiceInstance().getActionFormTypeClass(event);
    }
    
    /**
     * 获取表单事件的原始数据类型
     */
    public static Class<AbstractStateForm> getActionOriginTypeClass(String formName, String event) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        return instance.getServiceInstance().getActionOriginTypeClass(event);
    }
    
    /**
     * 获取表单事件的操作响应类型
     */
    public static Class<?> getActionReturnTypeClass(String formName, String event) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        return instance.getServiceInstance().getActionReturnTypeClass(event);
    }
    
    /**
     * 获取表单的默认检索实体类
     */
    public static StateFormNamedQuery<?> getFormDefaultQuery(String formName) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        return instance.getServiceInstance().getFormDefaultQuery();
    }
    
    /**
     * 获取表单的默认检索实体类
     */
    public static StateFormNamedQuery<?> getFormNamedQuery(String formName, String namedQuery) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        return instance.getServiceInstance().getFormNamedQuery(namedQuery);
    }

    /**
     * 检索表单
     */
    public static PagedList<?> listForm(String formName, JsonElement data) throws Exception {
        return getStateFormInstance(formName).getServiceInstance().listForm(data);
    }
    
    /**
     * 检索表单
     */
    public static PagedList<?> listForm(String formName, String namedQuery, JsonElement data) throws Exception {
        return getStateFormInstance(formName).getServiceInstance().listForm(namedQuery, data);
    }
    
    /**
     * 检索表单（包括分页用的总条数）
     */
    public static PagedListWithTotal<?> listFormWithTotal(String formName, JsonElement data)
                            throws Exception {
        return (PagedListWithTotal<?>) getStateFormInstance(formName).getServiceInstance().listFormWithTotal(data);
    }
    
    /**
     * 检索表单（包括分页用的总条数）
     */
    public static PagedListWithTotal<?> listFormWithTotal(String formName, String namedQuery, JsonElement data)
                            throws Exception {
        return (PagedListWithTotal<?>) getStateFormInstance(formName).getServiceInstance().listFormWithTotal(namedQuery, data);
    }
    
    /**
     * 获取指定表单的详情
     */
    public static AbstractStateForm getForm(String formName, long formId) throws Exception {
        return getStateFormInstance(formName).getServiceInstance().getForm(formId);
    }
    
    /**
     * 获取指定表单当前可执行操作
     */
    public static StateFormWithAction<?> getFormActions(String formName, long formId) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        AbstractStateFormServiceWithBaseDao<?, ?, ?> service = instance.getServiceInstance();
        StateFormWithAction<?> formActions = service.getFormActions(formId);
        resetActionsVisible(formActions.getActions(), instance);
        return formActions;
    }
    
    /**
     * 获取指定表单当前可执行操作
     */
    public static Map<String, String> getFormActionNames(String formName, long formId) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        AbstractStateFormServiceWithBaseDao<?, ?, ?> service = instance.getServiceInstance();
        Map<String, String> formActions = service.getFormActionNames(formId);
        removeHiddenActions(formActions, instance);
        return formActions;
    }
    
    /**
     * 获取指定表单的详情
     */
    public static StateFormWithAction<?> getFormNoActions(String formName, long formId) throws Exception {
        return getStateFormInstance(formName).getServiceInstance().getFormNoActions(formId);
    }
    
    /**
     * 获取指定表单的详情以及当前可执行操作
     */
    public static StateFormWithAction<?> getFormWithActions(String formName, long formId) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        StateFormWithAction<?> formWithActions = instance.getServiceInstance().getFormWithActions(formId);
        resetActionsVisible(formWithActions.getActions(), instance);
        return formWithActions;
    }
    
    /**
     * 设置流程实例的前端访问查询
     */
    public static void setContextQueries(String formName, List<NameValuePair> queries) throws Exception {
        getStateFormInstance(formName).getServiceInstance().setContextQueries(queries);
    }
    
    /**
     * 执行指定的表单操作
     */
    public static Object triggerAction(String formName, String formAction,
                                    AbstractStateForm formData, String message, Class<?> actionResult) throws Exception  {
        return getStateFormInstance(formName).getServiceInstance().triggerAction(formAction, formData, message, actionResult);
    }
    
    /**
     * 获取指定的操作准备数据。
     */
    public static AbstractStatePrepare triggerPrepare(String formName, String formAction, long formId,
            NameValuePair... params) throws Exception {
        return getStateFormInstance(formName).getServiceInstance().triggerPrepare(formAction, formId, params);
    }
    
    /**
     * 执行指定的表单创建操作
     */
    public static Long triggerSubmitAction(String formName, String formAction, AbstractStateForm formData) throws Exception  {
        return getStateFormInstance(formName).getServiceInstance().triggerSubmitAction(formAction, formData);
    }
    
    /**
     * 动态可选项搜索
     */
    public static List<? extends FieldOption> queryFieldTypeOptions(String fieldTypeKey, String keyword,
            String formName, Long formId) throws Exception {
        return queryFieldTypeOptions(formName, fieldTypeKey,
                CommonUtil.toJson(new FilterBasicKeyword(keyword, formName, formId)));
    }
    
    /**
     * 动态可选项搜索
     */
    public static List<? extends FieldOption> queryFieldTypeOptions(String formName, String fieldTypeKey,
            String filterJson) throws Exception {
        FieldType fieldTypeInstance = null;
        try {
            fieldTypeInstance = ClassUtil.getSingltonInstance(fieldTypeKey, FieldType.class);
        } catch (Exception ex) {
            log.warn("The field type key ({}) not found", ex);
            return null;
        }
        if (!FieldOptionsType.DYNAMIC.equals(fieldTypeInstance.getOptionsType())) {
            return null;
        }
        Class<? extends FieldOptionsFilter> filterClass = null;
        if ((filterClass = fieldTypeInstance.getDynamicFilterFormClass()) == null) {
            filterClass = FilterBasicKeyword.class;
        }
        FieldOptionsFilter filterData = HttpMessageConverter.toInstance(filterClass,
                CommonUtil.ifBlank(filterJson, "{}"));
        if (FilterBasicKeyword.class.isAssignableFrom(filterClass)) {
            ((FilterBasicKeyword) filterData).setFormJson(filterJson);
        }
        if (FilterAbstractFrom.class.isAssignableFrom(filterClass)) {
            ((FilterAbstractFrom) filterData).setFormName(formName);
        }
        return fieldTypeInstance.queryDynamicOptions(filterData);
    }
    
    /**
     * 根据动态可选项的值获取选项列表
     */
    public static List<? extends FieldOption> queryFieldTypeValues(String fieldTypeKey, String[] values)
            throws Exception {
        FieldType fieldTypeInstance = null;
        try {
            fieldTypeInstance = ClassUtil.getSingltonInstance(fieldTypeKey, FieldType.class);
        } catch (Exception ex) {
            log.warn("The field type key ({}) not found", ex);
            return null;
        }
        
        if (!FieldOptionsType.DYNAMIC.equals(fieldTypeInstance.getOptionsType())) {
            return null;
        }
        return fieldTypeInstance.queryDynamicValues((Object[]) values);
    }
    
    /**
     * 动态获取操作表单的定义
     */
    public static String queryFormTypeDefinition(String formTypeKey) throws Exception {
        Class<?> formClass = null;
        try {
            formClass = ClassUtil.loadClass(formTypeKey);
        } catch (Exception ex) {
            log.warn("The form type key ({}) not found", ex);
            return null;
        }
//      由于存在未实现任何接口或抽象类也为界面视图模型的情况，暂屏蔽该限制
//        if (!AbstractStateForm.class.isAssignableFrom(formClass)
//                && !FieldType.class.isAssignableFrom(formClass)
//                && !FieldOption.class.isAssignableFrom(formClass)
//                && !AbstractStateFormEventResultView.class.isAssignableFrom(formClass)
//                && !AbstractStateAsyncEeventView.class.isAssignableFrom(formClass)) {
//            log.warn("The form type ({}) is not child class of FieldOption, AbstractStateForm"
//                    + ", AbstractStateFormEventResultView or AbstractStateAsyncEeventView");
//            return null;
//        }
        FORM_DATA_CACHER.refresh(formTypeKey);
        return ClassUtil.classToJson(formClass).toString();
    }
    
    /**
     * 获取制定表单的状态可选值
     */
    public static List<? extends FieldOption> getStates(String formName) throws Exception  {
        StateFormInstance instance = getStateFormInstance(formName);
        AbstractStateFormServiceWithBaseDao<?, ?, ?> service = instance.getServiceInstance();
        return service.getStates();
    }
    
    /**
     * 查询指定表单界面的排版数据
     */
    public static CustomFormViewConfig getFieldCustomDefinition(String formClassPath) throws Exception {
        if (StringUtils.isBlank(formClassPath)) {
            throw new StateFormCustomFieldFormNotFoundException(formClassPath);
        }
        return getDao().queryAsObject(CustomFormViewConfig.class,
                "SELECT * FROM system_form_viewattrs WHERE class_path = ?", 
                new Object[] {formClassPath});
    }
    
    /**
     * 预览指定表单界面的排版信息变更
     */
    public static String previewFieldCustomDefinition(String form, String definition) throws Exception {
        if (StringUtils.isBlank(form)) {
            throw new StateFormCustomFieldFormNotFoundException(form);
        }
        Class<?> type = null;
        try {
            type = ClassUtil.loadClass(form);
        } catch (Exception e) {
            throw new StateFormCustomFieldFormNotFoundException(form);
        }
        String origin = ClassUtil.AttributesProccessor.setContextPreviewAttributes(form, definition);
        try {
            return ClassUtil.classToJson(type).toString();
        } finally {
            ClassUtil.AttributesProccessor.resetContextPreviewAttributes(origin);
        }
    }
    
    /**
     * 保存指定表单界面的排版信息
     */
    public static long saveFieldCustomDefinition(String formClassPath, String definition, Long revision) throws Exception {
         
         /**
          * 获取当前的版本信息, 并确认内容是否有更新
          */
         CustomFormViewConfig currentDefinition = getFieldCustomDefinition(formClassPath);
         if (currentDefinition != null && StringUtils.equals(currentDefinition.getFormAttrs(), definition)) {
             return currentDefinition.getRevision();
         }
         
         /**
          * 通过预览检测基本语法格式正确
          */
          previewFieldCustomDefinition(formClassPath, definition);
          
         /**
          * 如果版本未给定或小于1， 视为新增配置
          */
         if (revision == null || revision <= 0) {
             if (currentDefinition != null) {
                 throw new MessageException("入参数据错误，缺失版本信息，可能数据冲突");
             }
             getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                 "system_form_viewattrs", new ObjectMap()
                     .put("class_path", formClassPath)
                     .put("form_attrs", definition)
                     .put("revision", 1)
             ));
             revision = 1L;
         } 
         /**
          * 否则视为更新，同时检测版本一致性避免数据覆盖问题
          */
         else {
             if (getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                     "system_form_viewattrs", new ObjectMap()
                         .put("revision", revision + 1)
                         .put("form_attrs", definition)
                         .put("=class_path", formClassPath)
                         .put("=revision", revision)
                 )) <= 0) {
                 throw new MessageException("数据冲突，表单配置数据已被其他人更新，或已被删除");
             }
             revision += 1L;
         }
         /**
          * 配置信息做日志记录，确保历史版本可追溯
          */
         SimpleLogService.createLog("state.form.custom.definition", "edit", 
                         formClassPath, null, currentDefinition, new CustomFormViewConfig(formClassPath, definition, revision));
         return revision;
    }
    
    /**
     * 添加或更新表单的自定义属性
     */
    public static void saveFieldCustomAttribute(@NonNull StateFormFieldCustomAttribute attr) throws Exception {
        if (StringUtils.isAnyBlank(attr.getName(), attr.getDisplay(), attr.getDescription())) {
            throw new MessageException("自定义的通用流程表单或字段属性的名称、显示及描述字段均不可为空值");
        }
        if (attr.getId() == null) {
            if (getDao().queryAsObject(Long.class, "SELECT id FROM name = ?", 
                                            new Object[] {attr.getName()}) != null) {
                throw new MessageException(String.format(
                    "自定义的通用流程表单或字段属性(%s)已存在",
                    CommonUtil.ifNull(attr.getName(), "")
                ));
            }
            getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "system_form_fieldattrs", new ObjectMap()
                        .put("name", attr.getName())
                        .put("display", attr.getDisplay())
                        .put("description", attr.getDescription())));
            return;
        }
        getDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                "system_form_fieldattrs", new ObjectMap()
                    .put("id", attr.getId())
                    .put("=name", attr.getName())
                    .put("=display", attr.getDisplay())
                    .put("=description", attr.getDescription())));
    }
    
    /**
     * 删除表单的自定义属性
     */
    public static void removeFieldCustomAttribute(long id) throws Exception {
        getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_form_fieldattrs", new ObjectMap().put("=id", id)));
    }
    
    /**
     * 列举表单的自定义属性
     */
    public static List<StateFormFieldCustomAttribute> listFieldCustomAttribute() throws Exception {
        return getDao().queryAsList(StateFormFieldCustomAttribute.class,
                "SELECT * FROM system_form_fieldattrs ORDER BY name DESC");
    }
    
    /**
     * 获取表单的流程定义数据
     */
    public static StateFlowChartDefinition parseFormFlowDefinition(String formName, boolean keepUnChanged, Long formId) throws Exception {
        StateFormInstance instance = getStateFormInstance(formName);
        AbstractStateForm currentForm = null;
        if (formId != null) {
            currentForm = instance.getServiceInstance().getForm(formId);
        }
        AbstractStateFormServiceWithBaseDao<?, ?, ?> service = instance.getServiceInstance();
        Map<StateFlowChartNodeData, Set<StateFlowChartNodeData>> flowNodeData = service
                .parseFormFlowChartDefinition(keepUnChanged, currentForm);
        List<StateFlowChartLinkData> flowLinkData = new ArrayList<>();
        Set<StateFlowChartNodeData> currentStateChildNodes = new HashSet<>();
        for (Map.Entry<StateFlowChartNodeData, Set<StateFlowChartNodeData>> nodeEntry : flowNodeData.entrySet()) {
            boolean currentState = false;
            Set<StateFlowChartNodeData> nodeChildren = nodeEntry.getValue();
            if (nodeChildren == null || nodeChildren.size() <= 0) {
                continue;
            }
            if (nodeEntry.getKey().isCurrent()
                    && nodeEntry.getKey().getCategory() == StateFlowChartNodeData.Category.STATE) {
                currentState = true;
                currentStateChildNodes = nodeChildren;
            }
            for (StateFlowChartNodeData c : nodeChildren) {
                flowLinkData.add(new StateFlowChartLinkData(nodeEntry.getKey().getKey(), c.getKey(), currentState));
            }
        }
        if (currentStateChildNodes == null || currentStateChildNodes.isEmpty()) {
            return new StateFlowChartDefinition(flowNodeData.keySet(), flowLinkData);
        }
        Set<StateFlowChartNodeData> withApprovalNodeData = new HashSet<>();
        for (StateFlowChartNodeData nodeData : flowNodeData.keySet()) {
            if (currentStateChildNodes.contains(nodeData)
                    && nodeData.getCategory() == StateFlowChartNodeData.Category.ACTION) {
                withApprovalNodeData.add(new StateFlowChartNodeData(nodeData.getKey(), nodeData.getText(),
                        nodeData.getName(), service.getActionUserNameWithForm(nodeData.getName(), currentForm)));
            } else {
                withApprovalNodeData.add(nodeData);
            }
        }
        return new StateFlowChartDefinition(withApprovalNodeData, flowLinkData);
    }
    
    /**
     * 检索表单的变更日志
     */
    public static List<CommonSimpleLog> queryLogs(String formName, long formId, Long fromLogIndex)  throws Exception {
        return getStateFormInstance(formName).getServiceInstance().queryLogs(formId, fromLogIndex);
    }
    
    /**
     * 检索表单的讨论列表
     */
    public static List<CommonSimpleLog> queryComments(String formName, long formId, Long fromLogIndex)  throws Exception {
        return getStateFormInstance(formName).getServiceInstance().queryComments(formId, fromLogIndex);
    }
}
