package org.socyno.webfwk.state.abs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import lombok.Getter;
import lombok.NonNull;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.exec.StateFormActionNotFoundException;
import org.socyno.webfwk.state.exec.StateFormInvalidStatesException;
import org.socyno.webfwk.state.exec.StateFormNamedQueryNotFoundException;
import org.socyno.webfwk.state.model.CommonSimpleLog;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.service.SimpleLockService;
import org.socyno.webfwk.state.service.SimpleLogService;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormQueryDefinition;
import org.socyno.webfwk.state.util.StateFormRevision;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.HttpMessageConverter;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.service.AbstractSimpleLockService.CommonLockExecutor;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tmpl.EnjoyUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.state.util.StateFormActionDefinition.EventType;

import com.github.reinert.jjschema.v1.FieldOption;

@Getter
public abstract class AbstractStateFormServiceWithBaseDao<D extends L, L extends F, F extends AbstractStateFormBase>
        extends AbstractStateFormService<F> {
    
    protected abstract String getFormTable();
    
    protected abstract AbstractDao getFormBaseDao();
    
    protected abstract void fillExtraFormFields(Collection<? extends F> forms) throws Exception;
    
    private final Map<String, StateFormStateBaseEnum> states = new HashMap<>();
    
    private final Map<String, StateFormNamedQuery<? extends L>> queries = new HashMap<>();
    
    private final Map<String, AbstractStateAction<D, ?, ?>> actions = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public Class<D> getDetailFormClass() {
        return (Class<D>) ClassUtil.getActualParameterizedType(getClass(), AbstractStateFormServiceWithBaseDao.class,
                0);
    }
    
    @SuppressWarnings("unchecked")
    public Class<D> getDefaultFormClass() {
        return (Class<D>) ClassUtil.getActualParameterizedType(getClass(), AbstractStateFormServiceWithBaseDao.class,
                1);
    }
    
    @SafeVarargs
    protected final void fillExtraFormFields(F... forms) throws Exception {
        if (forms == null) {
            return;
        }
        fillExtraFormFields(Arrays.asList(forms));
    }
    
    @Override
    public <T extends F> T getForm(Class<T> clazz, long formId) throws Exception {
        T form = loadFormNoStateRevision(clazz, formId);
        fillExtraFormFields(form);
        return form;
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return new ArrayList<>(states.values());
    }
    
    @Override
    protected String[] getFormActionNames() {
        return actions.keySet().toArray(new String[0]);
    }
    
    @Override
    protected AbstractStateAction<? extends D, ?, ?> getFormAction(String event) {
        return actions.get(event);
    }
    
    public Map<String, StateFormNamedQuery<? extends L>> getFormQueries() {
        return Collections.unmodifiableMap(queries);
    }
    
    protected void setQuery(String name, StateFormNamedQuery<? extends L> query) {
        if (query == null) {
            queries.remove(name);
            return;
        }
        if (!getDefaultFormClass().equals(query.getResultClass())) {
            throw new MessageException(
                    String.format("Named query result class must be %s", getDefaultFormClass().getName()));
        }
        queries.put(name, query);
    }
    
    @SuppressWarnings("unchecked")
    protected void setQueries(StateFormQueryBaseEnum... queries) {
        if (queries == null || queries.length <= 0) {
            return;
        }
        for (StateFormQueryBaseEnum q : queries) {
            if (q == null) {
                continue;
            }
            setQuery(q.name(), (StateFormNamedQuery<? extends L>) q.getNamedQuery());
        }
    }
    
    private void setState(StateFormStateBaseEnum state) {
        states.put(state.getCode(), state);
    }
    
    /**
     * 
     * 获取给定值的状态码列表
     * 
     */
    protected String[] getStateCodes(StateFormStateBaseEnum... states) {
        if (states == null || states.length <= 0) {
            return new String[0];
        }
        List<String> result = new ArrayList<>(states.length);
        for (StateFormStateBaseEnum s : states) {
            if (s == null) {
                continue;
            }
            result.add(s.getCode());
        }
        return result.toArray(new String[0]);
    }
    
    /**
     * 
     * 获取排除给定值的状态码列表
     * 
     */
    protected String[] getStateCodesEx(StateFormStateBaseEnum... exclusions) {
        if (exclusions == null) {
            exclusions = new StateFormStateBaseEnum[0];
        }
        List<String> excodes = new ArrayList<>();
        for (StateFormStateBaseEnum e : exclusions) {
            if (e == null) {
                continue;
            }
            excodes.add(e.getCode());
        }
        List<String> result = new ArrayList<>(states.size());
        for (String s : states.keySet()) {
            if (s == null || excodes.contains(s)) {
                continue;
            }
            result.add(s);
        }
        return result.toArray(new String[0]);
    }
    
    protected void setStates(StateFormStateBaseEnum... states) {
        if (states == null || states.length <= 0) {
            return;
        }
        for (StateFormStateBaseEnum s : states) {
            setState(s);
        }
    }
    
    private void setAction(String event, AbstractStateAction<D, ?, ?> action) {
        if (action == null) {
            actions.remove(event);
            return;
        }
        actions.put(event, action);
    }
    
    @SuppressWarnings("unchecked")
    protected void setActions(StateFormEventClassEnum... events) {
        if (events == null || events.length <= 0) {
            return;
        }
        for (StateFormEventClassEnum e : events) {
            if (e == null) {
                continue;
            }
            if (e.getEventClass() == null) {
                setAction(e.getName(), null);
                continue;
            }
            try {
                setAction(e.getName(), (AbstractStateAction<D, ?, ?>) createInstance(e.getEventClass()));
            } catch (RuntimeException x) {
                throw (RuntimeException) x;
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        }
    }
    
    /**
     * 获取默认（第一个）的预定义查询
     * 
     * @return
     */
    public StateFormNamedQuery<? extends L> getFormDefaultQuery() {
        Map<String, StateFormNamedQuery<? extends L>> queries;
        if ((queries = getFormQueries()) == null || queries.isEmpty()) {
            return null;
        }
        for (String name : queries.keySet()) {
            if ("default".equalsIgnoreCase(name)) {
                return queries.get(name);
            }
        }
        for (StateFormNamedQuery<? extends L> query : queries.values()) {
            if (query != null) {
                return query;
            }
        }
        return null;
    }
    
    /**
     * 获取自定名称的查询
     */
    public StateFormNamedQuery<? extends L> getFormNamedQuery(String name) {
        Map<String, StateFormNamedQuery<? extends L>> queries;
        if (StringUtils.isBlank(name) || (queries = getFormQueries()) == null || queries.isEmpty()) {
            return null;
        }
        if (queries.containsKey(name)) {
            return queries.get(name);
        }
        if (queries.containsKey(name.toUpperCase())) {
            return queries.get(name.toUpperCase());
        }
        return queries.get(name.toLowerCase());
    }
    
    public String getFormIdField() {
        return "id";
    }
    
    public String getFormStateField() {
        return "state_form_status";
    }
    
    public String getFormRevisionField() {
        return "state_form_revision";
    }
    
    public String getFormCreatedAtField() {
        return "state_form_created_at";
    }
    
    public String getFormCreatedByField() {
        return "state_form_created_by";
    }
    
    public String getFormCreatedCodeByField() {
        return "state_form_created_code_by";
    }
    
    public String getFormCreatedNameByField() {
        return "state_form_created_name_by";
    }
    
    public String getFormUpdatedAtField() {
        return "state_form_updated_at";
    }
    
    public String getFormUpdatedByField() {
        return "state_form_updated_by";
    }
    
    public String getFormUpdatedCodeByField() {
        return "state_form_updated_code_by";
    }
    
    public String getFormUpdatedNameByField() {
        return "state_form_updated_name_by";
    }
    
    /**
     * 在流程实现中，建议是将表单状态及版本等基础字段与表单主内容存放与同一个表中，
     * 因此，在这里将状态及版本的信息设置为自动填充模式，在具体实现时请特别注意。
     */
    protected boolean autoFillStateRevision() {
        return true;
    }
    
    @Override
    protected void saveStateRevision(String event, long id, @NonNull String finalNewState, String originState) throws Exception {
        saveStateRevision(event, id, finalNewState, originState, null, new String[0]);
    }
    
    protected void saveStateRevision(String event, long id, @NonNull String finalNewState, String originState, ObjectMap customQueries, String... stateWhens)
            throws Exception {
        ObjectMap query = new ObjectMap();
        if (customQueries != null) {
            query.addAll(customQueries);
        }
        AbstractStateAction<?, ?, ?> action;
        if ((action = getExternalFormAction(event)) != null && EventType.Create.equals(action.getEventType())) {
            query.put("#".concat(getFormCreatedAtField()), "NOW()")
                    .put(getFormCreatedByField(), SessionContext.getUserId())
                    .put(getFormCreatedCodeByField(), SessionContext.getUsername())
                    .put(getFormCreatedNameByField(), SessionContext.getDisplay());
        }
        if (finalNewState.equals(originState)) {
            finalNewState = "";
        }
        if (action == null || StringUtils.isNotBlank(finalNewState) || !action.getStateRevisionChangeIgnored()) {
            query.put("#".concat(getFormUpdatedAtField()), "NOW()")
                    .put(getFormUpdatedByField(), SessionContext.getUserId())
                    .put(getFormUpdatedCodeByField(), SessionContext.getUsername())
                    .put(getFormUpdatedNameByField(), SessionContext.getDisplay());
        }
        if (stateWhens != null && stateWhens.length > 0) {
            query.put("=" + getFormStateField(), stateWhens);
        }
        if (StringUtils.isNotBlank(finalNewState)) {
            boolean found = false;
            List<? extends FieldOption> stateOptions = getStates();
            if ((stateOptions = getStates()) == null || stateOptions.isEmpty()) {
                throw new StateFormInvalidStatesException(getFormName(), finalNewState);
            }
            for (FieldOption option : stateOptions) {
                if (option == null) {
                    continue;
                }
                if (StringUtils.equals(finalNewState, option.getOptionValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new StateFormInvalidStatesException(getFormName(), finalNewState);
            }
            query.put(getFormStateField(), finalNewState);
        }
        query.put("=" + getFormIdField(), id).put("#".concat(getFormRevisionField()), getFormRevisionField() + " + 1");
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), query));
    }
    
    @Override
    protected Map<Long, StateFormRevision> loadStateRevision(Long[] formIds) throws Exception {
        List<StateFormRevision> data = null;
        if (formIds != null && formIds.length > 0) {
            data = getFormBaseDao().queryAsList(StateFormRevision.class,
                    String.format(
                            "SELECT DISTINCT %s id, %s status , %s revision,"
                                    .concat(" %s updatedAt, %s updatedCodeBy, %s updatedNameBy,")
                                    .concat(" %s createdAt, %s createdCodeBy, %s createdNameBy")
                                    .concat(" FROM %s WHERE %s IN(%s)"),
                            getFormIdField(), 
                            getFormStateField(), 
                            getFormRevisionField(), 
                            getFormUpdatedAtField(),
                            getFormUpdatedByField(), 
                            getFormUpdatedCodeByField(), 
                            getFormUpdatedNameByField(),
                            getFormCreatedAtField(), 
                            getFormCreatedByField(), 
                            getFormCreatedCodeByField(),
                            getFormCreatedNameByField(),  
                            getFormTable(),
                            getFormIdField(), 
                            CommonUtil.join("?", formIds.length, ", ")
                        ),
                    formIds);
        }
        if (data == null || data.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, StateFormRevision> result = new HashMap<>();
        for (StateFormRevision l : data) {
            result.put(l.getId(), l);
        }
        return result;
    }
    
    public List<F> queryFormWithStateRevision(String sql) throws Exception {
        return queryFormWithStateRevision(sql, null, null);
    }
    
    public List<F> queryFormWithStateRevision(String sql, Object[] args) throws Exception {
        return queryFormWithStateRevision(sql, args, null);
    }
    
    public List<F> queryFormWithStateRevision(String sql, Map<String, String> mapper) throws Exception {
        return queryFormWithStateRevision(sql, null, mapper);
    }
    
    public List<F> queryFormWithStateRevision(String sql, Object[] args, Map<String, String> mapper) throws Exception {
        return queryFormWithStateRevision(getFormClass(), sql, args, mapper);
    }
    
    public <T> List<T> queryFormWithStateRevision(Class<T> entityClass, String sql) throws Exception {
        return queryFormWithStateRevision(entityClass, sql, null, null);
    }
    
    public <T> List<T> queryFormWithStateRevision(Class<T> entityClass, String sql, Map<String, String> mapper)
            throws Exception {
        return queryFormWithStateRevision(entityClass, sql, null, mapper);
    }
    
    public <T> List<T> queryFormWithStateRevision(Class<T> entityClass, String sql, Object[] args) throws Exception {
        return queryFormWithStateRevision(entityClass, sql, args, null);
    }
    
    public <T> List<T> queryFormWithStateRevision(@NonNull Class<T> entityClass, String sql, Object[] args,
            Map<String, String> mapper) throws Exception {
        return getFormBaseDao().queryAsList(entityClass, sql, args, getAllFieldMapper(mapper));
    }
    
    @Override
    public <T extends F> List<T> listForm(@NonNull Class<T> clazz, @NonNull AbstractStateFormFilter<T> filter)
            throws Exception {
        return filter.apply(clazz);
    }
    
    public <T extends F> PagedList<T> listForm(@NonNull Class<T> resultClazz, @NonNull AbstractStateFormQuery query)
            throws Exception {
        List<T> resutSet = listForm(resultClazz, queryToFilter(resultClazz, query));
        return new PagedList<T>().setPage(query.getPage()).setLimit(query.getLimit())
                .setList(resutSet);
    }
    
    public PagedList<? extends F> listForm(@NonNull StateFormNamedQuery<? extends F> namedQuery,
            @NonNull Object condition) throws Exception {
        return listForm(namedQuery.getResultClass(),
                (AbstractStateFormQuery) HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
    }
    
    public PagedList<? extends F> listForm(@NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormDefaultQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), "<DEFAULT>");
        }
        return listForm(query, condition);
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集
     * 
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public PagedList<? extends F> listForm(@NonNull StateFormQueryBaseEnum namedQuery, @NonNull Object condition)
            throws Exception {
        StateFormNamedQuery<?> query;
        if ((query = namedQuery.getNamedQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery.name());
        }
        return listForm((StateFormNamedQuery<? extends F>) query, condition);
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集
     * 
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    public PagedList<? extends F> listForm(String namedQuery, @NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormNamedQuery(namedQuery)) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery);
        }
        return listForm(query, condition);
    }
    
    public <T extends F> PagedListWithTotal<T> listFormWithTotal(@NonNull Class<T> resultClazz,
            @NonNull AbstractStateFormQuery query) throws Exception {
        List<T> resutSet = listForm(resultClazz, queryToFilter(resultClazz, query));
        long total = (resutSet == null || resutSet.size() <= 0 || resutSet.size() >= query.getLimit())
                ? getListFormTotal(query)
                : (query.getOffset() + resutSet.size());
        return new PagedListWithTotal<T>().setPage(query.getPage()).setLimit(query.getLimit()).setTotal(total)
                .setList(resutSet);
    }
    
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull StateFormNamedQuery<? extends F> namedQuery,
            @NonNull Object condition) throws Exception {
        return listFormWithTotal(namedQuery.getResultClass(),
                (AbstractStateFormQuery) HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
    }
    
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormDefaultQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), "<DEFAULT>");
        }
        return listFormWithTotal(query, condition);
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集(同时返回结果集总条数)
     * 
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    public PagedListWithTotal<? extends F> listFormWithTotal(String namedQuery, @NonNull Object condition)
            throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormNamedQuery(namedQuery)) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery);
        }
        return listFormWithTotal(query, condition);
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集(同时返回结果集总条数)
     * 
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull StateFormQueryBaseEnum namedQuery,
            @NonNull Object condition) throws Exception {
        StateFormNamedQuery<?> query;
        if ((query = namedQuery.getNamedQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery.name());
        }
        return listFormWithTotal((StateFormNamedQuery<? extends F>) query, condition);
    }
    
    public long getListFormTotal(@NonNull AbstractStateFormQuery query) throws Exception {
        AbstractSqlStatement sql = query.prepareSqlTotal();
        return getFormBaseDao().queryAsObject(Long.class, sql.getSql(), sql.getValues());
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集的总条目数
     * 
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    public long getListFormTotal(@NonNull String namedQuery, @NonNull Object condition) throws Exception {
        return getListFormTotal(getFormNamedQuery(namedQuery), condition);
    }
    
    public long getListFormTotal(@NonNull StateFormNamedQuery<?> namedQuery, @NonNull Object condition)
            throws Exception {
        return getListFormTotal(HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
    }
    
    /**
     * 获取表单详情数据的 SQL 模板。
     * 格式适用 jFinal 的 enjoy 模板，可用参数分别为 :
     * 
     * <pre>
     *       formTable   : 表名称
     *       formIdField : ID 字段名
     *       formIdValue : ID 字段值
     * 默认模板语句 ： SELECT * FROM #(formTable) WHERE #(formIdField)=#(formIdValue)
     * </pre>
     */
    protected String loadFormSqlTmpl() {
        return "SELECT * FROM #(formTable) WHERE #(formIdField)=#(formIdValue)";
    }
    
    protected Map<String, String> getAllFieldMapper(Map<String, String> mapper) {
        Map<String, String> mappAll = new HashMap<String, String>();
        if (mapper != null) {
            mappAll.putAll(mapper);
        }
        mappAll.put(getFormIdField(), "id");
        mappAll.put(getFormStateField(), "state");
        mappAll.put(getFormRevisionField(), "revision");
        mappAll.put(getFormCreatedAtField(), "createdAt");
        mappAll.put(getFormCreatedByField(), "createdBy");
        mappAll.put(getFormCreatedCodeByField(), "createdCodeBy");
        mappAll.put(getFormCreatedNameByField(), "createdNameBy");
        mappAll.put(getFormUpdatedAtField(), "updatedAt");
        mappAll.put(getFormUpdatedByField(), "updatedBy");
        mappAll.put(getFormUpdatedCodeByField(), "updatedCodeBy");
        mappAll.put(getFormUpdatedNameByField(), "updatedNameBy");
        return mappAll;
    }
    
    @Override
    protected <T extends F> T loadFormNoStateRevision(Class<T> clazz, long formId) throws Exception {
        return getFormBaseDao().queryAsObject(clazz,
                EnjoyUtil.format(loadFormSqlTmpl(),
                        new ObjectMap().put("formTable", getFormTable())
                                .put("formIdField", getFormIdField())
                                .put("formIdValue", formId).asMap()),
                new Object[0], getAllFieldMapper(getExtraFieldMapper(clazz, null)));
    }
    
    @Override
    public D getForm(long formId) throws Exception {
        return getForm(getDetailFormClass(), formId);
    }
    
    @Override
    protected void triggerPostHandle(String event, String finalNewState, Object result, F originForm, AbstractStateFormInput form, String message)
            throws Exception {
        super.triggerPostHandle(event, finalNewState, result, originForm, form, message);
        if (form.getId() == null) {
            return;
        }
        String logEventName = event;
        AbstractStateAction<F, ?, ?> eventAction;
        if ((eventAction = getExternalFormAction(event)) != null
                && (EventType.Comment.equals(eventAction.getEventType()))) {
            logEventName = AbstractStateCommentAction.getFormLogEvent();
        }
        AbstractStateFormInput changedForm = form;
        if (eventAction != null && !EventType.Delete.equals(eventAction.getEventType())) {
            changedForm = getForm(form.getId());
        }
        /**
         * 异步通知
         */
        SystemNotifyService.sendAsync(String.format("system.state.form:%s:%s", getFormName(), event),
                new ObjectMap().put("formEvent", event)
                        .put("formName", getFormName())
                        .put("formService", this)
                        .put("originForm", originForm)
                        .put("changedForm", changedForm)
                        .put("eventMessage", message)
                        .asMap(),
                SystemNotifyService.NOEXCEPTION_TMPL_NOTFOUD);
        /**
         * 记录日志
         */
        /* revision not changed, no message provided, disable log */
        if (StringUtils.isBlank(message) && originForm != null && changedForm != null
                && changedForm.getRevision().equals(originForm.getRevision())) {
            return;
        }
        SimpleLogService.createLog(logEventName, getFormName(), form.getId(), message, originForm, changedForm);
    }
    
    /**
     * 检索表单的日志记录
     * 
     * @param formId
     * @param fromLogIndex
     */
    public List<CommonSimpleLog> queryLogs(long formId, Long fromLogIndex) throws Exception {
        return SimpleLogService.queryLogExcludeOperationTypes(getFormName(), formId,
                new String[] { AbstractStateCommentAction.getFormLogEvent() }, fromLogIndex);
    }
    
    /**
     * 检索表单的首条日志记录
     * 
     * @param formId
     */
    public CommonSimpleLog queryFirstLog(long formId) throws Exception {
        return SimpleLogService.getFirstLog(getFormName(), formId);
    }
    
    /**
     * 检索表单的最新的日志记录
     * 
     * @param formId
     */
    public CommonSimpleLog queryLatestLog(long formId) throws Exception {
        return SimpleLogService.getLatestLog(getFormName(), formId);
    }
    
    /**
     * 检索表单的讨论列表
     * 
     * @param formId
     * @param fromLogIndex
     */
    public List<CommonSimpleLog> queryComments(long formId, Long fromLogIndex) throws Exception {
        return SimpleLogService.queryLogIncludeOperationTypes(getFormName(), formId,
                new String[] { AbstractStateCommentAction.getFormLogEvent() }, fromLogIndex);
    }
    
    @Override
    protected void triggerExceptionHandle(String event, F originForm, AbstractStateFormInput form, Throwable ex)
            throws Exception {
        super.triggerExceptionHandle(event, originForm, form, ex);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T superTriggerActionWithTransactional(String event, AbstractStateFormInput form, String message,
            Class<T> clazz) throws Exception {
        final Object[] result = new Object[] { null };
        getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet r, Connection c) throws Exception {
                result[0] = superTriggerAction(event, form, message, clazz);
            }
        });
        return (T) result[0];
    }
    
    private <T> T superTriggerAction(String event, AbstractStateFormInput form, String message, Class<T> clazz)
            throws Exception {
        return super.triggerAction(event, form, message, clazz);
    }
    
    public void triggerAction(String event, AbstractStateFormInput form, String message) throws Exception {
        triggerAction(event, form, message, void.class);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T triggerAction(final String event, final AbstractStateFormInput form, final String message,
            final Class<T> clazz) throws Exception {
        AbstractStateAction<F, ?, ?> action;
        if ((action = getExternalFormAction(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        
        /* 确认无内容变更的操作, 不添加分布锁 */
        if ((EventType.Create.equals(action.getEventType()) || action.ensureNoStateChange())
                && action.getStateRevisionChangeIgnored()) {
            return superTriggerActionWithTransactional(event, form, message, clazz);
        }
        
        /* 加锁且事务处理, 表单创建使用表单全局锁 */
        Long lockObjectId = form.getId();
        if (EventType.Create.equals(action.getEventType())) {
            lockObjectId = -1000000L;
        }
        final Object[] result = new Object[] { null };
        SimpleLockService.getInstance().lockAndRun(
                String.format("system.state.form:%s", getFormName()), 
                lockObjectId, String.format("system.state.event:%s", event), new CommonLockExecutor() {
            @Override
            public void execute() throws Exception {
                result[0] = superTriggerActionWithTransactional(event, form, message, clazz);
            }
        });
        return (T) result[0];
    }
    
    /**
     * 获取最终查询的字段和属性的映射关系
     */
    protected Map<String, String> getExtraFieldMapper(Class<?> resultClass, Map<String, String> fieldMapper) {
        return fieldMapper;
    }
    
    /**
     * 获取表单的外部事件定义
     * 
     */
    public final List<StateFormQueryDefinition> getFormQueryDefinition() throws Exception {
        Map<String, StateFormNamedQuery<? extends L>> queries;
        List<StateFormQueryDefinition> definitions = new ArrayList<>();
        if ((queries = getFormQueries()) != null) {
            for (Map.Entry<String, StateFormNamedQuery<? extends L>> query : queries.entrySet()) {
                if (query == null) {
                    continue;
                }
                definitions
                        .add(StateFormQueryDefinition.fromStateQuery(getFormName(), query.getKey(), query.getValue()));
            }
        }
        return definitions;
    }
    
    @Override
    public void saveMultiChoiceCoveredTargetScopeIds(String event, AbstractStateFormInput form, String scopeType,
            String... coveredTargetScopeIds) throws Exception {
        if (coveredTargetScopeIds == null || coveredTargetScopeIds.length <= 0) {
            return;
        }
        for (String scopeId : coveredTargetScopeIds) {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_form_multiple_choice_status",
                    new ObjectMap().put("event", event)
                            .put("form_id", form.getId())
                            .put("form_name", getFormName())
                            .put("=scope_id", scopeId)
                            .put("=scope_type", scopeType)
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getTokenUsername())
                            .put("state_form_revision", form.getRevision())
                            .put("created_by", SessionContext.getUserId())
                            .put("created_name_by", SessionContext.getDisplay())));
        }
    }
    
    /**
     * select s.scope_id from system_form_multiple_choice_status s
     * WHERE
     * s.event = ? AND s.form_id = ? AND s.form_name = ? AND s.fail = 0
     */
    @Multiline
    private static final String SQL_QUERY_MULTIPLE_APPROVAL_BY_APPROVE = "X";
    
    @Override
    public List<Long> queryMultipleChoiceCoveredTargetScopeIds(String event, AbstractStateFormInput form) throws Exception {
        return getFormBaseDao().queryAsList(Long.class, SQL_QUERY_MULTIPLE_APPROVAL_BY_APPROVE,
                new Object[] { event, form.getId(), getFormName() });
    }
    
    /**
     * UPDATE system_form_multiple_choice_status s
     * SET s.fail = 1
     * WHERE
     * ( s.form_id = ? AND s.form_name = ? )
     * %s
     */
    @Multiline
    private static final String SQL_UPDATE_MULTIPLE_APPROVAL = "X";
    
    @Override
    public void cleanMultipleChoiceTargetScopeIds(String[] clearEvents, AbstractStateFormInput form) throws Exception {
        if (clearEvents != null && clearEvents.length > 0) {
            getFormBaseDao()
                    .executeUpdate(
                            String.format(SQL_UPDATE_MULTIPLE_APPROVAL,
                                    String.format("AND s.event IN (%s)",
                                            CommonUtil.join("?", clearEvents.length, ","))),
                            ArrayUtils.addAll(new Object[] { form.getId(), getFormName() }, (Object[]) clearEvents));
        } else {
            getFormBaseDao().executeUpdate(String.format(SQL_UPDATE_MULTIPLE_APPROVAL, ""),
                    new Object[] { form.getId(), getFormName() });
        }
    }
    
    protected <T extends F> AbstractStateFormFilter<T> queryToFilter(final @NonNull Class<T> clazz,
            final @NonNull AbstractStateFormQuery query) {
        return new AbstractStateFormFilter<T>() {
            @Override
            public List<T> apply(Class<T> clazz) throws Exception {
                AbstractSqlStatement sql = query.prepareSqlQuery();
                List<T> list = queryFormWithStateRevision(clazz, sql.getSql(), sql.getValues(),
                        getExtraFieldMapper(clazz, query.getFieldMapper()));
                fillExtraFormFields(list);
                return list;
            }
        };
    }
}
