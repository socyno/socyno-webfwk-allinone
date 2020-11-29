package org.socyno.webfwk.state.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.socyno.webfwk.state.exec.StateFormNamedQueryNotFoundException;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormQueryDefinition;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.HttpMessageConverter;
import org.socyno.webfwk.util.exception.AbstractMethodUnimplimentedException;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;

import com.github.reinert.jjschema.v1.FieldOption;

import lombok.NonNull;

public abstract class AbstractStateFormServiceWithBaseDaoV2<F extends AbstractStateForm>
        extends AbstractStateFormServiceWithBaseDao<F> {
    
    private final Map<String, StateFormStateBaseEnum> states = new HashMap<>();
    
    private final Map<String, StateFormNamedQuery<? extends F>> queries = new HashMap<>();
    
    private final Map<String, AbstractStateAction<F, ?, ?>> actions = new HashMap<>();
    
    @Override
    public List<? extends FieldOption> getStates() {
        return new ArrayList<>(states.values());
    }
    
    @Override
    protected Map<String, AbstractStateAction<F, ?, ?>> getFormActions() {
        return Collections.unmodifiableMap(actions);
    }
    
    public Map<String, StateFormNamedQuery<? extends F>> getFormQueries() {
        return Collections.unmodifiableMap(queries);
    }
    
    protected void setQuery(String name, StateFormNamedQuery<? extends F> query) {
        if (query == null) {
            queries.remove(name);
            return;
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
            if (q.getNamedQuery() == null || !getFormClass().isAssignableFrom(q.getNamedQuery().getResultClass())) {
                throw new MessageException(String.format("Named query result class must be extended from class %s .",
                        getFormClass().getName()));
            }
            setQuery(q.name(), (StateFormNamedQuery<? extends F>) q.getNamedQuery());
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
    
    protected void setStates(StateFormStateBaseEnum ...states) {
        if (states ==  null || states.length <= 0) {
            return;
        }
        for (StateFormStateBaseEnum s : states) {
             setState(s);
        }
    }
    
    private void setAction(String event, AbstractStateAction<F, ?, ?> action) {
        if (action == null) {
            actions.remove(event);
            return;
        }
        actions.put(event, action);
    }
    
    @SuppressWarnings("unchecked")
    protected void setActions(StateFormEventClassEnum ...events) {
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
                setAction(e.getName(), (AbstractStateAction<F, ?, ?>) createInstance(e.getEventClass()));
            } catch (RuntimeException x) {
                throw (RuntimeException) x;
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        }
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        throw new  AbstractMethodUnimplimentedException();
    }
    
    @Override
    protected boolean autoFillStateRevision() {
        throw new AbstractMethodUnimplimentedException();
    }
    
    /**
     * 获取默认（第一个）的预定义查询
     * @return
     */
    @Override
    public StateFormNamedQuery<? extends F> getFormDefaultQuery() {
        Map<String, StateFormNamedQuery<? extends F>> queries;
        if ((queries = getFormQueries()) == null || queries.isEmpty()) {
            return null;
        }
        for (String name : queries.keySet()) {
            if ("default".equalsIgnoreCase(name)) {
                return queries.get(name);
            }
        }
        for (StateFormNamedQuery<? extends F>query : queries.values()) {
            if (query != null) {
                 return query;
            }
        }
        return null;
    }
    
    /**
     * 获取自定名称的查询
     * @param name
     * @return
     */
    @Override
    public StateFormNamedQuery<? extends F> getFormNamedQuery(String name) {
        Map<String, StateFormNamedQuery<? extends F>> queries;
        if ((queries = getFormQueries()) == null || queries.isEmpty()) {
            return null;
        }
        return queries.get(name);
    }
    
    @Override
    public final List<StateFormQueryDefinition> getFormQueryDefinition() throws Exception {
        Map<String, StateFormNamedQuery<? extends F>> queries;
        List<StateFormQueryDefinition> definitions = new ArrayList<>();
        if ((queries = getFormQueries()) != null) {
            for (Map.Entry<String, StateFormNamedQuery<? extends F>> query : queries.entrySet()) {
                if (query == null) {
                    continue;
                }
                definitions
                        .add(StateFormQueryDefinition.fromStateQuery(getFormName(), query.getKey(), query.getValue()));
            }
        }
        return definitions;
    }

    public F getForm(long formId) throws Exception {
        return getForm(getFormClass(), formId);
    }
    
    @Override
    public <T extends F> T getForm(Class<T> clazz, long formId) throws Exception {
        T form = loadFormNoStateRevision(clazz, formId);
        fillExtraFormFields(form);
        return form;
    }
    
    protected abstract void fillExtraFormFields(Collection<? extends F> forms) throws Exception;
    
    @SafeVarargs
    protected final void fillExtraFormFields(F ...forms) throws Exception {
        if (forms == null) {
            return;
        }
        fillExtraFormFields(Arrays.asList(forms));
    }

    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public PagedList<? extends F> listForm(@NonNull StateFormQueryBaseEnum namedQuery, @NonNull Object condition) throws Exception {
        StateFormNamedQuery<?> query;
        if ((query = namedQuery.getNamedQuery()) == null || !getFormClass().isAssignableFrom(query.getResultClass())) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery.name());
        }
        return listForm((StateFormNamedQuery<? extends F>)query, condition);
    }
    
    @Override
    public PagedList<? extends F> listForm(String namedQuery, @NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormNamedQuery(namedQuery)) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery);
        }
        return listForm(query, condition);
    }
    
    @Override
    public PagedList<? extends F> listForm(@NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormDefaultQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), "<DEFAULT>");
        }
        return listForm(query, condition);
    }
    
    public PagedList<? extends F> listForm(@NonNull StateFormNamedQuery<? extends F> namedQuery, @NonNull Object condition) throws Exception {
        return listForm(namedQuery.getResultClass(), (AbstractStateFormQuery)HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集(同时返回结果集总条数)
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull StateFormQueryBaseEnum namedQuery, @NonNull Object condition) throws Exception {
        StateFormNamedQuery<?> query;
        if ((query = namedQuery.getNamedQuery()) == null || !getFormClass().isAssignableFrom(query.getResultClass())) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery.name());
        }
        return listFormWithTotal((StateFormNamedQuery<? extends F>)query, condition);
    }
    
    @Override
    public PagedListWithTotal<? extends F> listFormWithTotal(String namedQuery, @NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormNamedQuery(namedQuery)) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), namedQuery);
        }
        return listFormWithTotal(query, condition);
    }
    
    @Override
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull Object condition) throws Exception {
        StateFormNamedQuery<? extends F> query;
        if ((query = getFormDefaultQuery()) == null) {
            throw new StateFormNamedQueryNotFoundException(getFormName(), "<DEFAULT>");
        }
        return listFormWithTotal(query, condition);
    }
    
    public PagedListWithTotal<? extends F> listFormWithTotal(@NonNull StateFormNamedQuery<? extends F> namedQuery, @NonNull Object condition) throws Exception {
        return listFormWithTotal(namedQuery.getResultClass(), (AbstractStateFormQuery)HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
    }
    
    /**
     * 根据预定义的查询名称和条件数据，获取表单结果集的总条目数
     * @param namedQuery 查询名称
     * @param condition  查询条件数据
     * @return
     * @throws Exception
     */
    @Override
    public long getListFormTotal(@NonNull String namedQuery, @NonNull Object condition) throws Exception {
        return getListFormTotal(getFormNamedQuery(namedQuery), condition);
    }
    
    public long getListFormTotal(@NonNull StateFormNamedQuery<?> namedQuery, @NonNull Object condition) throws Exception {
        return getListFormTotal(HttpMessageConverter.toInstance(namedQuery.getQueryClass(), condition));
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
    
    @Override
    public <T extends F> List<T> listForm(@NonNull Class<T> clazz, @NonNull AbstractStateFormFilter<T> filter) throws Exception {
        return filter.apply(clazz);
    }
    
    public <T extends F> PagedList<T> listForm(@NonNull Class<T> resultClazz, @NonNull AbstractStateFormQuery query)
                        throws Exception {
        List<T> resutSet = listForm(resultClazz, queryToFilter(resultClazz, query));
        return new PagedList<T>().setPage(query.getPage()).setLimit(query.getLimit())
                            .setList(query.processResultSet(resultClazz, resutSet));
    }
    
    public <T extends F> PagedListWithTotal<T> listFormWithTotal(@NonNull Class<T> resultClazz,
                        @NonNull AbstractStateFormQuery query) throws Exception {
        List<T> resutSet = listForm(resultClazz, queryToFilter(resultClazz, query));
        long total = (resutSet == null || resutSet.size() <= 0 || resutSet.size() >= query.getLimit())
                                    ? getListFormTotal(query) : (query.getOffset() + resutSet.size());
       return new PagedListWithTotal<T>().setPage(query.getPage()).setLimit(query.getLimit()).setTotal(total)
                               .setList(query.processResultSet(resultClazz, resutSet));
    }
    
    @Override
    protected <T extends AbstractStateForm> PagedList<T> listFormX(@NonNull Class<T> resultClazz, @NonNull AbstractStateFormQuery filter)
                        throws Exception {
        throw new  AbstractMethodUnimplimentedException();
    }
    
    @Override
    protected <T extends AbstractStateForm> PagedListWithTotal<T> listFormWithTotalX(@NonNull Class<T> resultClazz,
                        @NonNull AbstractStateFormQuery filter) throws Exception {
        throw new  AbstractMethodUnimplimentedException();
    }
    
}
