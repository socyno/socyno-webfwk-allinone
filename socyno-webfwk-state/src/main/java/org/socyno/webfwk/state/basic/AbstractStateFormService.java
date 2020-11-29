package org.socyno.webfwk.state.basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.exec.StateFormActionDeclinedException;
import org.socyno.webfwk.state.exec.StateFormActionMessageRequiredException;
import org.socyno.webfwk.state.exec.StateFormActionNotFoundException;
import org.socyno.webfwk.state.exec.StateFormActionReturnException;
import org.socyno.webfwk.state.exec.StateFormEventResultNullException;
import org.socyno.webfwk.state.exec.StateFormNotFoundException;
import org.socyno.webfwk.state.exec.StateFormRevisionChangedException;
import org.socyno.webfwk.state.exec.StateFormRevisionNotFoundException;
import org.socyno.webfwk.state.exec.StateFormSubmitEventDefinedException;
import org.socyno.webfwk.state.exec.StateFormSubmitEventResultException;
import org.socyno.webfwk.state.exec.StateFormSubmitEventTargetException;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.model.CommonFlowChartNodeData;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.util.StateFormActionDefinition;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormRevision;
import org.socyno.webfwk.state.util.StateFormWithAction;
import org.socyno.webfwk.util.context.RunableWithSessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.DataUtil;
import org.socyno.webfwk.state.util.StateFormActionDefinition.EventType;

import com.github.reinert.jjschema.v1.FieldOption;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStateFormService<S extends AbstractStateForm> {
    
    /**
     * 存储前端的请求参数，以便更好的实现前后端交互的定制化
     **/
    private final static ThreadLocal<List<NameValuePair>> CONTEXT_QUERIES = new ThreadLocal<>();

    public abstract String getFormName();

    protected abstract void saveStateRevision(long formId, String state) throws Exception;

    protected abstract Map<Long, StateFormRevision> loadStateRevision(Long[] formIds) throws Exception;

    protected abstract <T extends S> T loadFormNoStateRevision(Class<T> clazz, long formId) throws Exception;

    public abstract List<? extends FieldOption> getStates();

    protected abstract Map<String, AbstractStateAction<S, ?, ?>> getFormActions();

    public boolean isSubmitAction(String event) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if ((action = getExternalFormAction(event)) == null) {
            return false;
        }
        return EventType.Submit.equals(action.getEventType());
    }

    @SuppressWarnings("unchecked")
    public Class<S> getFormClass() {
        return (Class<S>)ClassUtil.getActualParameterizedType(
                             getClass(), AbstractStateFormService.class, 0);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends S> getRealFormClass() {
        Class<? extends S> formClass = getFormClass();
        try {
            Class<?> realFormClass = getClass().getMethod("getForm", long.class).getReturnType();
            if (formClass.isAssignableFrom(realFormClass)) {
                formClass = (Class<? extends S>) realFormClass;
            }
        } catch(Exception ex) {
            log.error(ex.toString(), ex);
        }
        return formClass;
    }

    protected <T> T createInstance(@NonNull Class<T> clazz) throws InvocationTargetException, IllegalAccessException,
                                    InstantiationException, IllegalArgumentException, NoSuchMethodException {
        Class<?> dclazz;
        if ((dclazz = clazz.getDeclaringClass()) != null && !Modifier.isStatic(clazz.getModifiers())
                && dclazz.isAssignableFrom(this.getClass())) {
            return clazz.getDeclaredConstructor(dclazz).newInstance(this);
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 获取完成的表单数据，包括状态、版本及创建信息
     *
     */
    public S getForm(long formId) throws Exception {
        return getForm(getFormClass(), formId);
    }
    
    public <T extends S> T getForm(Class<T> clazz, long formId) throws Exception {
       T form;
        if ((form = loadFormNoStateRevision(clazz, formId)) == null) {
            throw new StateFormNotFoundException(getFormName(), formId);
        }
        StateFormRevision s;
        if ((s = loadStateRevision(formId)) == null) {
            throw new StateFormRevisionNotFoundException(getFormName(), formId);
        }
        form.setState(s.getStateFormStatus());
        form.setRevision(s.getStateFormRevision());
        return form;
    }

    /**
     * 获取表单及其结构类名称
     *
     */
    public StateFormWithAction<S> getFormNoActions(long formId) throws Exception {
        return getFormWithActions(getForm(formId), null);
    }

    /**
     * 获取表单的可执行操作
     *
     */
    public StateFormWithAction<S> getFormActions(long formId) throws Exception {
        return getFormWithActions(getForm(formId), getFormActions(true, null), true);
    }

    /**
     * 获取表单的可执行操作
     *
     */
    public Map<String, String> getFormActionNames(long formId) throws Exception {
        final Map<String, String> formActions = new HashMap<>();
        checkFormWithActions(getForm(formId), getFormActions(true, null), new FormActionsProcessor() {
            @Override
            public void enabled(S form, String action, AbstractStateAction<S, ?, ?> definition) throws Exception {
                formActions.put(action, definition.getDisplay());
            }
        });
        return formActions;
    }

    /**
     * 组装表单实体以及可执行操作
     *
     */
    public StateFormWithAction<S> getFormWithActions(long formId) throws Exception {
        return getFormWithActions(getForm(formId), getFormActions(true, null));
    }

    /**
     * 组装表单及其对应的可执行操作
     *
     */
    protected StateFormWithAction<S> getFormWithActions(S form, Map<String, AbstractStateAction<S, ?, ?>> actions)
                throws Exception {
        return getFormWithActions(form, actions, false);
    }

    protected abstract class FormActionsProcessor {

        public abstract void enabled (S form, String action, AbstractStateAction<S, ?, ?> definition) throws Exception;

        public void disabled (S form, String action, AbstractStateAction<S, ?, ?> definition) throws Exception {

        }
    }

    /**
     * 获取状态的显示名称
     */
    public String getStateDisplay(String state) {
        for (FieldOption option : getStates()) {
            if (StringUtils.equals(option.getOptionValue(), state)) {
                return option.getOptionDisplay();
            }
        }
        return null;
    }

    protected void checkFormWithActions(S form, Map<String, AbstractStateAction<S, ?, ?>> actions,
            @NonNull FormActionsProcessor processor) throws Exception {
        if (form == null) {
            return;
        }
        if (actions != null && actions.size() > 0) {
            for (Entry<String, AbstractStateAction<S, ?, ?>> a : actions.entrySet()) {
                String actionName = a.getKey();
                AbstractStateAction<S, ?, ?> actionDef = a.getValue();
                if (actionDef.sourceContains(form.getState()) && checkAction(actionName, form)) {
                    processor.enabled(form, actionName, actionDef);
                } else {
                    processor.disabled(form, actionName, actionDef);
                }
            }
        }
    }

    protected StateFormWithAction<S> getFormWithActions(final S form, Map<String, AbstractStateAction<S, ?, ?>> actions,
                        boolean actionsOnly) throws Exception {
        final List<StateFormActionDefinition> formActions = new ArrayList<>();
        checkFormWithActions(form, actions, new FormActionsProcessor() {
            @Override
            public void enabled(S form, String action, AbstractStateAction<S, ?, ?> definition) throws Exception {
                StateFormActionDefinition fromAction = StateFormActionDefinition.fromStateAction(action, getFormName(), definition);
                if (definition.isDynamicEvent()) {
                    fromAction.setFormClass(definition.getDynamicFormDefinition(action, form));
                }
                formActions.add(fromAction);
            }
        });
        return new StateFormWithAction<S>(actionsOnly ? null : form, formActions);
    }

    /**
     * 筛选表单。在完成筛选后自动填充状态及版本等元数据。
     */
    public final List<S> listForm(@NonNull AbstractStateFormFilter<S> filter) throws Exception {
        return listForm(getFormClass(), filter);
    }

    /**
     * 筛选表单。在完成筛选后自动填充状态及版本等元数据。
     */
    public <T extends S> List<T> listForm(@NonNull Class<T> clazz, @NonNull AbstractStateFormFilter<T> filter) throws Exception {
        List<T> list = filter.apply(clazz);
        if (list != null && list.size() > 0) {
            Set<Long> formIds = new HashSet<>();
            for (S l : list) {
                formIds.add(l.getId());
            }
            Map<Long, StateFormRevision> srs = loadStateRevision(formIds.toArray(new Long[0]));
            for (S f : list) {
                StateFormRevision sr;
                if ((sr = srs.get(f.getId())) != null) {
                    f.setState(sr.getStateFormStatus());
                    f.setRevision(sr.getStateFormRevision());
                }
            }
        }
        return list;
    }

    /**
     * 获取指定的事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getFormAction(String name, boolean external) throws Exception {
        Map<String, AbstractStateAction<S, ?, ?>> actions;
        if (StringUtils.isBlank(name) || (actions = getFormActions(external, null)).isEmpty()) {
            return null;
        }
        return actions.get(name);
    }

    /**
     * 获取指定的外部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getExternalFormAction(String name) throws Exception {
        return getFormAction(name, true);
    }

    /**
     * 获取指定的外部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getExternalFormAction(@NonNull StateFormEventClassEnum event) throws Exception {
        return getExternalFormAction(event.getName());
    }

    /**
     * 获取指定的外部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getExternalFormAction(@NonNull StateFormEventBaseEnum event) throws Exception {
        return getExternalFormAction(event.getName());
    }

    /**
     * 获取指定的内部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getInternalFormAction(String name) throws Exception {
        return getFormAction(name, false);
    }

    /**
     * 获取指定的内部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getInternalFormAction(@NonNull StateFormEventClassEnum event) throws Exception {
        return getInternalFormAction(event.getName());
    }

    /**
     * 获取指定的内部事件
     *
     */
    protected AbstractStateAction<S, ?, ?> getInternalFormAction(@NonNull StateFormEventBaseEnum event) throws Exception {
        return getInternalFormAction(event.getName());
    }

    /**
     * 是否支持注释功能
     *
     */
    public boolean supportCommentFormAction()  throws Exception {
        Map<String, AbstractStateAction<S, ?, ?>> actions;
        if ((actions = getFormActions(true, null)).isEmpty()) {
            return false;
        }
        for (AbstractStateAction<S, ?, ?> action : actions.values()) {
            if (action instanceof AbstractStateCommentAction) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取表单的外部事件定义
     *
     */
    public final List<StateFormActionDefinition> getExternalFormActionDefinition()  throws Exception {
        String event;
        AbstractStateAction<S, ?, ?> action;
        List<StateFormActionDefinition> actions = new ArrayList<>();
        for (Map.Entry<String,AbstractStateAction<S,?,?>> e : getFormActions(true, null).entrySet()) {
            event = e.getKey();
            action = e.getValue();
            if (action == null) {
                continue;
            }
            actions.add(StateFormActionDefinition.fromStateAction(event, getFormName(), action));
        }
        return actions;
    }

//    /**
//     * 解析状态机的事件定义，生成流程数据，用于绘制流程图
//     */
//    public final Map<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> parseFormFlowChartDefinition(
//            boolean removeUnChangeStateNode) throws Exception {
//        return parseFormFlowChartDefinition(removeUnChangeStateNode, null);
//    }

    /**
     * 解析状态机的事件定义，生成流程数据，用于绘制流程图
     */
    @SuppressWarnings("unchecked")
    public final Map<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> parseFormFlowChartDefinition(
            boolean keepUnChanged, AbstractStateForm form) throws Exception {
        /**
         * 加载当前的状态信息
         */
        StateFormRevision stateRevision = null;
        if (form != null) {
            stateRevision = new StateFormRevision().setId(form.getId())
                .setStateFormRevision(form.getRevision())
                .setStateFormStatus(form.getState());
        }
        /**
         * 将事件及状态的流转关系转化为基础流程数据
         */
        AbstractStateAction<S, ?, ?> action;
        Map<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> baseFlowData = new HashMap<>();
        for (Map.Entry<String, AbstractStateAction<S,?,?>> e : getFormActions(true, null).entrySet()) {
            action = e.getValue();
            if (action == null) {
                continue;
            }
            if (form != null && !action.flowMatched((S)form)) {
                continue;
            }
            CommonFlowChartNodeData actionNode = new CommonFlowChartNodeData(e.getKey(), action);
            if (!baseFlowData.containsKey(actionNode)) {
                baseFlowData.put(actionNode, new HashSet<>());
            }
            for (String source : action.getSourceStates()) {
                CommonFlowChartNodeData sourceNode = new CommonFlowChartNodeData(source, getStateDisplay(source), stateRevision);
                if (!baseFlowData.containsKey(sourceNode)) {
                    baseFlowData.put(sourceNode, new HashSet<>());
                }
                baseFlowData.get(actionNode).add(sourceNode);
            }
            parseStateChoice(action.getTargetState(), actionNode, baseFlowData, stateRevision, (S)form);
        }
        /**
         * 构造流程图所需的数据结构;
         * 流程数据结果的 key 为父节点， values 为子节点
         */
        CommonFlowChartNodeData stateUnchangedNode = null;
        Set<CommonFlowChartNodeData> noParentStateNodes = new HashSet<>(); 
        Map<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> resultFlowData = new HashMap<>();
        for (Map.Entry<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> flowEntry : baseFlowData.entrySet()) {
            CommonFlowChartNodeData childNode = flowEntry.getKey();
            Set<CommonFlowChartNodeData> parentNodes = flowEntry.getValue();
            if (CommonFlowChartNodeData.Category.STATE.equals(flowEntry.getKey().getCategory())
                    && parentNodes.isEmpty()) {
                noParentStateNodes.add(childNode);
                continue;
            }
            if (!resultFlowData.containsKey(childNode)) {
                resultFlowData.put(flowEntry.getKey(), new HashSet<>());
            }
            for(CommonFlowChartNodeData parentNode : parentNodes) {
                if (!resultFlowData.containsKey(parentNode)) {
                    resultFlowData.put(parentNode,new HashSet<>());
                }
                resultFlowData.get(parentNode).add(flowEntry.getKey());
                if (stateUnchangedNode == null) {
                    if (CommonFlowChartNodeData.Category.UNCHANGED.equals(parentNode.getCategory())) {
                        stateUnchangedNode = parentNode;
                    } else if (CommonFlowChartNodeData.Category.UNCHANGED.equals(flowEntry.getKey().getCategory())) {
                        stateUnchangedNode = flowEntry.getKey();
                    }
                }
            }
        }
        /**
         * 移除所有无父节点的状态节点及其子节点信息
         */
        for (CommonFlowChartNodeData node : noParentStateNodes) {
            resultFlowData.remove(node);
        }
        if (keepUnChanged) {
            return resultFlowData;
        }
        /**
         * 移除非状态流转的流程节点，如表单编辑或相关数据查询事件等节点;
         * 首先移除 "状态保持不变" 的流程节点，然后在移除所有非状态类且子节点为空的节点
         */
        resultFlowData.remove(stateUnchangedNode);
        for (Set<CommonFlowChartNodeData> children: resultFlowData.values()) {
            children.remove(stateUnchangedNode);
        }
        while (true) {
            boolean hasNodeDeleted = false;
            for (CommonFlowChartNodeData parentNode : resultFlowData.keySet().toArray(new CommonFlowChartNodeData[0])) {
                Set<CommonFlowChartNodeData> childrenNodes = resultFlowData.get(parentNode);
                if (childrenNodes.isEmpty() && !CommonFlowChartNodeData.Category.STATE.equals(parentNode.getCategory())
                        && !CommonFlowChartNodeData.Category.STATE_CURRENT.equals(parentNode.getCategory())) {
                    hasNodeDeleted = true;
                    resultFlowData.remove(parentNode);
                    for (Set<CommonFlowChartNodeData> children : resultFlowData.values()) {
                        children.remove(parentNode);
                    }
                }
            }
            if (!hasNodeDeleted) {
                break;
            }
        }

        return resultFlowData;
    }

    /**
     * 递归解析事件的状态选择器，生成流程节点数据
     */
    private void parseStateChoice(final AbstractStateChoice targetChoice, final CommonFlowChartNodeData parentNode,
                                  final Map<CommonFlowChartNodeData, Set<CommonFlowChartNodeData>> flowData,
                                  final StateFormRevision stateRevision, final S form) {
        /**
         * 最终的简单状态值处理
         */
        if (targetChoice == null || targetChoice.isSimple()) {
            String stateValue = targetChoice == null ? null : targetChoice.getTargetState();
            CommonFlowChartNodeData stateNode = new CommonFlowChartNodeData(stateValue, getStateDisplay(stateValue),
                                                                        stateRevision);
            if (!flowData.containsKey(stateNode)) {
                flowData.put(stateNode, new HashSet<>());
            }
            flowData.get(stateNode).add(parentNode);
            return;
        }
        /**
         * 状态选择器的递归，遍历其 true 和 false 的目标
         */
        CommonFlowChartNodeData choiceNode = new CommonFlowChartNodeData(targetChoice);
        if (!flowData.containsKey(choiceNode)) {
            flowData.put(choiceNode, new HashSet<>());
        }
        flowData.get(choiceNode).add(parentNode);
        for (boolean yesNo : new boolean[]{true, false}) {
            /**
             * 目标指向为 null， 意味着不会发生状态变化，此时做丢弃处理
             */
            AbstractStateChoice yesNoChoice = yesNo ? targetChoice.getTrueState() : targetChoice.getFalseState();
            if (yesNoChoice == null) {
                continue;
            }
            if(form != null && !targetChoice.flowMatched(form, yesNo)) {
                continue;
            }
            /**
             * 对于简单选择器，为确保 YesNo 节点的唯一性，随机生成唯一键
             */
            CommonFlowChartNodeData yesNoNode = yesNoChoice.isSimple()
                    ? new CommonFlowChartNodeData(yesNo, DataUtil.randomGuid())
                    : new CommonFlowChartNodeData(yesNo, yesNoChoice.getClass().getName());
            if (!flowData.containsKey(yesNoNode)) {
                flowData.put(yesNoNode, new HashSet<>());
            }
            flowData.get(yesNoNode).add(choiceNode);

            /**
             * 对于简单选择器，直接创建其对应的状态节点
             */
            CommonFlowChartNodeData targetNode = yesNoChoice.isSimple()
                    ? new CommonFlowChartNodeData(yesNoChoice.getTargetState(),
                            getStateDisplay(yesNoChoice.getTargetState()), stateRevision)
                    : new CommonFlowChartNodeData(yesNoChoice);
            if (!flowData.containsKey(targetNode)) {
                flowData.put(targetNode, new HashSet<>());
            }
            flowData.get(targetNode).add(yesNoNode);

            /**
             * 非简单选择器，则需要继续递归，生成流程节点数据
             */
            if (!yesNoChoice.isSimple()) {
                parseStateChoice(yesNoChoice, yesNoNode, flowData, stateRevision, form);
            }
        }
    }

    /**
     * 定义流程的预定义特殊事件注册信息
     */
    @SuppressWarnings("serial")
    public Map<String, String> getFormPredefinedExtraEvents() {
        return new HashMap<String, String>() {{
            put(getFormAccessEventName(), "访问");
        }};
    }

    /**
     * 获取特定类型（外部或内部）的操作
     *
     * @param external
     *            true  表示外部 : 创建事件或有source states的事件;
     *            false 表示内部 : 非创建事件, 且无 source states定义
     * @param targetState
     *            限制仅获取目标状态为该值操作
     * @return
     */
    private Map<String, AbstractStateAction<S, ?, ?>> getFormActions(boolean external, String targetState) throws Exception {
        Map<String, AbstractStateAction<S, ?, ?>> actions;
        if ((actions = getFormActions()) == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        String name;
        String[] sourceStates;
        boolean isExternalEvent;
        AbstractStateChoice targetStateChoice;
        AbstractStateAction<S, ?, ?> definition;
        boolean targetStateReuired = StringUtils.isNotBlank(targetState);

        /**
         * false 返回内部事件，true 返回外部事件。
         * 如果指定了目标状态的情况下，只返回明确为该目标状态的事件。
         */
        Map<String, AbstractStateAction<S, ?, ?>> targetActions = new HashMap<>();
        for (Entry<String, AbstractStateAction<S, ?, ?>> action : actions.entrySet()) {
            name = action.getKey();
            definition = action.getValue();
            if (StringUtils.isBlank(name) || definition == null) {
                continue;
            }
            sourceStates = definition.getSourceStates();
            targetStateChoice = definition.getTargetState();
            isExternalEvent = sourceStates.length > 0;

            // 如果指定目标状态的选择，不符合的一律忽略
            if (targetStateReuired) {
                if (targetStateChoice == null || !targetStateChoice.isSimple()
                              || !targetState.equals(targetStateChoice.getTargetState())) {
                    continue;
                }
            }
            if (external) {
                // 表单创建事件无 source states, 但属于外部事件 
                if (EventType.Submit.equals(definition.getEventType())) {
                    targetActions.put(name, definition);
                    if (definition.getSourceStates().length > 0) {
                      throw new StateFormSubmitEventDefinedException(getFormName(), name);
                    }
                } else if (isExternalEvent) {
                    targetActions.put(name, definition);
                }
            } else if (!isExternalEvent) {
                targetActions.put(name, definition);
            }
        }
        return targetActions;
    }

    /**
     * 获取特定目标状态的内部操作
     *
     * @param targetState
     *            限制仅获取目标状态为该值操作
     * @return
     */
    protected Map<String, AbstractStateAction<S, ?, ?>> getInternalFromActions(String targetState)  throws Exception {
        if (StringUtils.isBlank(targetState)) {
            return Collections.emptyMap();
        }
        return getFormActions(false, targetState);
    }

    /**
     * 确认给定的操作是否允许执行
     */
    public boolean checkAction(String event, long formId) throws Exception {
        return checkAction(event, getForm(formId));
    }

    /**
     * 确认给定的操作是否允许执行
     */
    public boolean checkAction(String event, S form) throws Exception {
        AbstractStateAction<S, ?, ?> action = null;
        if ((action = getExternalFormAction(event)) == null || EventType.Submit.equals(action.getEventType())) {
            log.warn("表单({})未定义此外部操作({})。", getFormName(), event);
            return false;
        }

        String sourceState;
        if (!action.sourceContains(sourceState = form == null ? null : form.getState())) {
            log.warn("表单({})的外部操作({})不可从指定的状态({})上执行。", getFormName(), event, sourceState);
            return false;
        }
        try {
            Authority authority;
            if ((authority = action.getAuthority()) == null) {
                log.warn("表单({})操作({})未声明授权信息。", getFormName(), event);
                return false;
            }
            if (createInstance(authority.rejecter()).check(form)) {
                return false;
            }
            if (createInstance(authority.checker()).check(form)) {
                return true;
            }
            Long scopeTargetId = null;
            long allScopeTargetIds[] = null;
            if (authority.value().checkScopeId()) {
                if (!AuthorityScopeIdNoopParser.class.equals(authority.parser())){
                    if ((scopeTargetId = createInstance(authority.parser()).getAuthorityScopeId(form)) == null) {
                        throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                    }
                    allScopeTargetIds = new long[] {scopeTargetId};
                }
                if (!AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser())){
                    if ((allScopeTargetIds = createInstance(authority.multipleParser()).getAuthorityScopeIds(form)) == null || allScopeTargetIds.length <= 0) {
                        throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                    }
                }
            }
            if (allScopeTargetIds == null) {
                return PermissionService.hasFormEventPermission(authority.value(), getFormName(), event, null);
            }
            if (authority.multipleChoice() || authority.multipleOneAllowed()) {
                for (long targetId : allScopeTargetIds) {
                    if (PermissionService.hasFormEventPermission(authority.value(), getFormName(), event, targetId)) {
                        return true;
                    } 
                }
                return false;
            }
            for (long targetId : allScopeTargetIds) {
                if (!PermissionService.hasFormEventPermission(authority.value(), getFormName(), event, targetId)) {
                   return false;
                } 
            }
            return true;
        } catch(Exception e) {
            log.error(String.format("表单(%s)的外部操作(%s)可执行检测程序异常。", getFormName(), event), e);
            return false;
        }
    }
    
    /**
     * 针对表单的创建事件。
     *
     */
    public boolean checkSubmitAction(String event, AbstractStateForm form) throws Exception {
        AbstractStateAction<S, ?, ?> action = null;
        if ((action = getExternalFormAction(event)) == null || !EventType.Submit.equals(action.getEventType())) {
            log.warn("表单({})未定义此创建操作({})。", getFormName(), event);
            return false;
        }

        try {
            Authority authority;
            if ((authority = action.getAuthority()) == null) {
                log.warn("表单({})操作({})未声明授权信息。", getFormName(), event);
                return false;
            }
            /**
             * form 为 null 值，视为事前评估，此时只要在授权范围(SopeType)
             * 上发现有任一标的(ScopeId)被授予该权限，即视为可执行该操作
             */
            if (form == null) {
                return PermissionService.hasFormEventAnyPermission(getFormName(), event);
            }
            if (createInstance(authority.rejecter()).check(form)) {
                return false;
            }
            if (createInstance(authority.checker()).check(form)) {
                return true;
            }
            Long scopeTargetId = null;
            long allScopeTargetIds[] = null;
            if (authority.value().checkScopeId()) {
                if (!AuthorityScopeIdNoopParser.class.equals(authority.parser())){
                    if ((scopeTargetId = createInstance(authority.parser()).getAuthorityScopeId(form)) == null) {
                        throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                    }
                    allScopeTargetIds = new long[] {scopeTargetId};
                }
                if (!AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser())){
                    if ((allScopeTargetIds = createInstance(authority.multipleParser()).getAuthorityScopeIds(form)) == null
                             || allScopeTargetIds.length <= 0) {
                        throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                    }
                }
            }
            if (allScopeTargetIds == null) {
                return PermissionService.hasFormEventPermission(authority.value(), getFormName(), event, null);
            }
            if (allScopeTargetIds.length < 1) {
                return false;
            }
            for (Long targetId : allScopeTargetIds) {
                if (!PermissionService.hasFormEventPermission(authority.value(), getFormName(), event, targetId)) {
                    return false;
                }
            }
            return true;
        } catch(Exception e) {
            log.error(String.format("表单(%s)的创建操作(%s)可执行检测程序异常。", getFormName(), event), e);
            return false;
        }
    }

    /**
     * 获取当前流程指定事件上的授权人员清单
     */
    public Long[] getActionUserIds(String event, S form) throws Exception {
       return ArrayUtils.toObject(getFormExternalActionUserIds(event, form));
    }

    /**
     * 获取当前流程指定外部事件（不包括创建事件）上的授权人员清单
     */
    public long[] getFormExternalActionUserIds(String event, S form) throws Exception {
        AbstractStateAction<S, ?, ?> action = null;
        if ((action = getExternalFormAction(event)) == null || EventType.Submit.equals(action.getEventType())) {
            throw new MessageException(String.format("表单(%s)未定义此创建操作(%s)", getFormName(), event));
        }
        Authority authority;
        if ((authority = action.getAuthority()) == null) {
            throw new MessageException(String.format("表单(%s)操作(%s)未声明授权信息", getFormName(), event));
        }
        Long scopeTargetId = null;
        long[] allScopeTargetIds = null;
        if (authority.value().checkScopeId()) {
            if (!AuthorityScopeIdNoopParser.class.equals(authority.parser())){
                if ((scopeTargetId = createInstance(authority.parser()).getAuthorityScopeId(form)) == null) {
                    throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                }
                allScopeTargetIds = new long[] {scopeTargetId};
            }
            if (!AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser())){
                if ((allScopeTargetIds = createInstance(authority.multipleParser()).getAuthorityScopeIds(form)) == null
                         || allScopeTargetIds.length <= 0) {
                    throw new MessageException(String.format("表单(%s)操作(%s)解析授权标的失败", getFormName(), event));
                }
            }
        }
        if (!authority.value().checkScopeId() || authority.multipleChoice() || authority.multipleOneAllowed()) {
            return ConvertUtil.asNonNullUniquePrimitiveLongArray(PermissionService.queryFormEventUsers(authority.value(), getFormName(), event, false, allScopeTargetIds)
                    .toArray(new Long[0]));
        }
        return  ConvertUtil.asNonNullUniquePrimitiveLongArray(PermissionService.queryFormEventUsers(authority.value(), getFormName(), event, true, allScopeTargetIds)
                .toArray(new Long[0]));
    }
    
    /**
     * 获取当前表单指定事件上的授权人员清单(返回的为用户的显示名称)
     */
    @SuppressWarnings("unchecked")
    public List<String> getActionUserNameWithForm(String event, AbstractStateForm form) throws Exception {
        return getUserNamesByIds(getFormExternalActionUserIds(event, (S)form));
    }
    
    private List<String> getUserNamesByIds(long[] userIds) throws Exception {
        if (userIds == null || userIds.length <= 0) {
            return null;
        }
        List<OptionSystemUser> optionUsers;
        if ((optionUsers = ClassUtil.getSingltonInstance(FieldSystemUser.class)
                .queryDynamicValues(ArrayUtils.toObject(userIds))) == null || optionUsers.isEmpty()) {
            return null;
        }
        List<String> userNames = new ArrayList<>();
        for (OptionSystemUser option : optionUsers) {
            if (option == null) {
                continue;
            }
            userNames.add(option.getDisplay());
        }
        return userNames;
    }
    
    /**
     * 操作执行前的检查。
     * 
     * @return 如检查失败，抛出异常；否则返回当前的表单实体。
     */
    protected S triggerPreHandle(String event, AbstractStateForm form) throws Exception {
        S originForm = null;
        AbstractStateAction<S, ?, ?> action;
        if ((action = getExternalFormAction(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        log.info("Try to check form action trigger : event = {}, class = {}, eventType = {}"
                                + ", ensureNoStateChange = {}, stateRevisionChangeIgnored = {}", 
                        event, action.getClass().getName(), action.getEventType(), 
                        action.ensureNoStateChange(), action.getStateRevisionChangeIgnored());
        if (EventType.Submit.equals(action.getEventType())) {
            if (!checkSubmitAction(event, form)) {
                throw new StateFormActionDeclinedException(getFormName(), event);
            }
            return null;
        }
        originForm = getForm(form.getId());
        if (originForm.getRevision() == null) {
            throw new StateFormRevisionNotFoundException(getFormName(), originForm.getId(), true);
        }
        if (!action.getStateRevisionChangeIgnored()) {
            if (form.getRevision() == null) {
                throw new StateFormRevisionNotFoundException(getFormName(), form.getId());
            }
            if (form.getRevision().longValue() != originForm.getRevision().longValue()) {
                throw new StateFormRevisionChangedException(form, form.getRevision(), originForm.getRevision());
            }
        }
        if (!checkAction(event, originForm)) {
            throw new StateFormActionDeclinedException(getFormName(), event);
        }
        return originForm;
    }
    
    /**
     * 获取指定的表单事件数据。
     */
    public AbstractStatePrepare triggerPrepare(String event, long formId, NameValuePair[] params) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if ((action = getExternalFormAction(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        log.info("Try to prepare form action trigger : event = {}, class = {}, eventType = {}"
                                + ", ensureNoStateChange = {}, stateRevisionChangeIgnored = {}"
                                + ", prepareRequired = {}", 
                        event, action.getClass().getName(), action.getEventType(), 
                        action.ensureNoStateChange(), action.getStateRevisionChangeIgnored(),
                        action.prepareRequired());
        return action.prepareForm(this, event, formId > 0 ? getForm(formId) : null, params);
    }
    
    /**
     * 执行指定的表单创建操作，并返回新表单编号。
     */
    public Long triggerSubmitAction(String event, AbstractStateForm form) throws Exception {
        return triggerSubmitAction(event, form, "");
    }
    
    /**
     * 执行指定的表单创建操作，并返回新表单编号。
     */
    public Long triggerSubmitAction(String event, AbstractStateForm form, String message) throws Exception {
        if (!isSubmitAction(event)) {
            throw new MessageException("给定的事件(%s)非表单创建事件。") ;
        }
        return triggerAction(event, form, message, Long.class);
    }
    
    /**
     * 执行指定的操作，忽略返回值。
     */
    public void triggerAction(String event, AbstractStateForm form) throws Exception {
        triggerAction(event, form, "");
    }
    
    /**
     * 执行指定的操作，忽略返回值。
     */
    public void triggerAction(String event, AbstractStateForm form, String message) throws Exception {
        if (isSubmitAction(event)) {
            triggerAction(event, form, message, Long.class);
            return;
        }
        triggerAction(event, form, message, void.class);
    }
    
    /**
     * 执行指定操作，必须确保事件定义的返回值与给定的返回值类型匹配。
     */
    public <T> T triggerAction(String event, AbstractStateForm form, Class<T> clazz) throws Exception {
        return triggerAction(event, form, "", clazz);
    }
    
    /**
     * 执行指定操作，必须确保事件定义的返回值与给定的返回值类型匹配。
     */
    @SuppressWarnings("unchecked")
    public <T> T triggerAction(final String event, final AbstractStateForm form, final String message, final Class<T> clazz) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if ((action = getExternalFormAction(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        
        /**
         * 前置校验，确保可执行操作
         */
        S originForm = triggerPreHandle(event, form);
        try {
            /**
             * 校验 message 是否设置
             */
            if (action.messageRequired() != null && action.messageRequired() && StringUtils.isBlank(message)) {
                throw new StateFormActionMessageRequiredException(getFormName(), event);
            }
            
            /**
             * 校验要求字段确保值不为空，且值在预期的选项范围内
             */
            if (form != null) {
                ClassUtil.checkFormRequiredAndOpValue(form, true /* 忽略只读字段 */,
                        AbstractStateAction.getInternalFields() /* 忽略内部字段 */);
            }
            
            /**
             * 校验返回数据类型 
             */
            Class<?> returnTypeClass = action.getReturnTypeClass();
            if (clazz != null && !void.class.equals(clazz) && !clazz.isAssignableFrom(returnTypeClass)) {
                log.error("Defined return class of action {} is {}, but require {}",
                                            event, returnTypeClass, clazz);
                throw new StateFormActionReturnException(getFormName(), event);
            }
            
            /**
             * 执行预定义处理方法 
             */
            final Object result = action.handleForm(this, event, originForm, form, message);
            
            /**
             * 异步任务处理
             */
            if (AbstractStateAsyncEeventView.class.isAssignableFrom(returnTypeClass)) {
                new Thread(new RunableWithSessionContext() {
                    @Override
                    public void exec() {
                        try {
                            /* 重要：此处的休眠时为确保操作结束后，线程才开始运行，
                             * 否则该创建异步任务的事务还未提交，导致等待时的异常 */
                            Thread.sleep(1000);
                            if (((AbstractStateAsyncEeventView)result).waitingForFinished(event, message, originForm, form)) {
                                psotHandleForm(event, message, result, originForm, form);
                            }
                        } catch (Exception e) {
                            log.error(e.toString(), e);
                        }
                    }
                }).start();
            } else {
                psotHandleForm(event, message, result, originForm, form);
            }
            if (clazz == null || void.class.equals(clazz)) {
                return (T)null;
            }
            return (T)result;
        } catch (Throwable ex) {
            triggerExceptionHandle(event, originForm, form, ex);
            throw ex;
        }
    }
    
    public abstract void saveMultiChoiceCoveredTargetScopeIds(String event, AbstractStateForm form, String scopeType, long ...coverdTargetScopeIds) throws Exception;
    
    public abstract List<Long> queryMultipleChoiceCoveredTargetScopeIds(String event, AbstractStateForm form)  throws Exception;;
    
    public abstract void cleanMultipleChoiceTargetScopeIds(String[] clearEvents, AbstractStateForm form) throws Exception;;
    
    private void psotHandleForm(String event, String message, Object result, S originForm, AbstractStateForm form) throws Exception {
        /**
         * 设置目标状态
         */
        String newState = null;
        AbstractStateChoice targetState;
        AbstractStateAction<S, ?, ?> action = getExternalFormAction(event);
        Authority authority = action.getAuthority();
        boolean isSubmitEvent = EventType.Submit.equals(action.getEventType());
        if ((targetState = action.getTargetState()) != null) {
            newState = targetState.getTargetState(form, originForm, this, event);
        }
        if (result == null && !action.allowHandleReturnNull()) {
            throw new StateFormEventResultNullException(getFormName(), event);
        }
        if (isSubmitEvent) {
            if (StringUtils.isBlank(newState)) {
                throw new StateFormSubmitEventTargetException(getFormName(), event);
            }
            if (result != null) {
                if (!(result instanceof Long) || ((Long)result) <= 0) {
                    throw new StateFormSubmitEventResultException(getFormName(), event);
                }
            }
            form.setState(newState);
            form.setId((Long)result);
            form.setRevision(1L);
        } else {
            form.setState(CommonUtil.ifBlank(newState, originForm.getState()));
            if (!AuthorityScopeIdNoopMultipleParser.class.equals(authority.multipleParser()) && authority.multipleChoice()) {
                // 当前用户有权限的业务系统清单
                Long[] coverScopeIds = PermissionService.queryFormEventScopeTargetIds(AuthorityScopeType.Subsystem, getFormName(), event);
                // 当前表单涉及的业务系统清单
                long[] multipleScopeTargetIds = createInstance(authority.multipleParser())
                        .getAuthorityScopeIds(originForm == null ? form : originForm);
                long[] finalCoverScopeTargetIds = coverScopeIds != null ? ConvertUtil.asNonNullUniquePrimitiveLongArray(coverScopeIds)
                        : multipleScopeTargetIds;
                saveMultiChoiceCoveredTargetScopeIds(event,form, AuthorityScopeType.Subsystem.name(), finalCoverScopeTargetIds);
                List<Long> allCoverScopeTargetIds = queryMultipleChoiceCoveredTargetScopeIds(event, form);
                if (allCoverScopeTargetIds != null) {
                    for (Long r : allCoverScopeTargetIds) {
                        if (!ArrayUtils.contains(finalCoverScopeTargetIds, r)) {
                            allCoverScopeTargetIds = null;
                            break;
                        }
                    }
                }
                if (allCoverScopeTargetIds == null || allCoverScopeTargetIds.size() <= 0) {
                    form.setState(originForm.getState());
                }
            }

            if(!AuthorityScopeIdNoopMultipleCleaner.class.equals(authority.multipleCleaner())) {
                String[] clearEvents = createInstance(authority.multipleCleaner()).getEventsToClean();
                cleanMultipleChoiceTargetScopeIds(clearEvents,form);
            }
        }
        String eventAppendMessage;
        if (result != null && (result instanceof AbstractStateFormEventMessageAppender)
                && StringUtils.isNotBlank(eventAppendMessage = 
                    ((AbstractStateFormEventMessageAppender)result).getEventAppendMessage())) {
            if (StringUtils.isBlank(message)) {
                message = eventAppendMessage;
            } else {
                message = String.format("%s\r\n%s", message, eventAppendMessage);
            }
        }
        
        log.info("Form action has been handled : form = {}, id = {}, event = {}, target = {}",
                            getFormName(), form.getId(), event, newState);
        /**
         * 处理内部事件，即状态到达或离开时的自动触发的事件
         */
        if ((isSubmitEvent && form.getId() != null) || originForm != null) {
            AbstractStateAction<S, ?, ?> eventAction;
            Map<String, AbstractStateAction<S, ?, ?>> stateActions;
            /**
             * 处理状态离开事件
             */
            if (originForm != null && (stateActions = getInternalFromActions(originForm.getState())).size() > 0) {
                for (Entry<String, AbstractStateAction<S, ?, ?>> stateAction :stateActions.entrySet()) {
                    if (((eventAction = stateAction.getValue()) instanceof AbstractStateLeaveAction)
                            && (!StringUtils.equals(originForm.getState(), form.getState())
                                    || ((AbstractStateLeaveAction<?>)eventAction).executeWhenNoStateChanged())) {
                        eventAction.handleForm(this, event, stateAction.getKey(), originForm, form, message);
                    }
                }
            }
            /**
             * 处理状态进入事件
             */
            if ((stateActions = getInternalFromActions(form.getState())).size() > 0) {
                for (Entry<String, AbstractStateAction<S, ?, ?>> stateAction :stateActions.entrySet()) {
                    if (((eventAction = stateAction.getValue()) instanceof AbstractStateEnterAction)
                            && (isSubmitEvent || !StringUtils.equals(originForm.getState(), form.getState())
                                    || ((AbstractStateEnterAction<?>)eventAction).executeWhenNoStateChanged())) {
                        eventAction.handleForm(this, event, stateAction.getKey(), originForm, form, message);
                    }
                }
            }
        }
        /**
         * 保存状态并回调预定义后处理方法
         */
        triggerPostHandle(event, result, originForm, form, message);
    }
    
    protected void triggerPostHandle(String event, Object result, S originForm, AbstractStateForm form, String message) throws Exception {
        /**
         * 否则只针对有明确的表单状态发生变化，或未明确忽略变更的进行保存。
         * 
         * 同时调用 post 回调
         */
        AbstractStateAction<S, ?, ?> absAction = getExternalFormAction(event);
        String originState = EventType.Submit.equals(absAction.getEventType())
                                ? null : originForm.getState();
        if (!StringUtils.equals(originState, form.getState())
                || !absAction.getStateRevisionChangeIgnored()) {
            if (!EventType.Delete.equals(absAction.getEventType()) && form.getId() != null) {
                saveStateRevision(form.getId(), StringUtils.equals(originState, form.getState()) ? "" : form.getState());
            }
        }
        try {
            absAction.postForm(this, event, result, originForm, form, message);
        } catch (Exception e) {
            log.error(e.toString() ,e);
        }
    }
    
    protected void triggerExceptionHandle(String event, S originForm, AbstractStateForm form, Throwable ex) throws Exception {
        
    }
    
    protected StateFormRevision loadStateRevision(long formId) throws Exception {
        Map<Long, StateFormRevision> sr = loadStateRevision(new Long[] {formId});
        if (sr == null || sr.size() != 1) {
            return null;
        }
        return sr.values().iterator().next();
    }

    public final Class<AbstractStateForm> getActionFormTypeClass(String event) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if((action = getFormActions().get(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        return action.getFormTypeClass();
    }
    
    public final Class<AbstractStateForm> getActionOriginTypeClass(String event) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if((action = getFormActions().get(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        return action.getOriginTypeClass();
    }

    public final Class<?> getActionReturnTypeClass(String event) throws Exception {
        AbstractStateAction<S, ?, ?> action;
        if((action = getFormActions().get(event)) == null) {
            throw new StateFormActionNotFoundException(getFormName(), event);
        }
        return action.getReturnTypeClass();
    }
    
    public String getFormEventKey(String event) {
        return getFormEventKey(getFormName(), event);
    }
    
    public static String getFormEventKey(String formName, String formEvent) {
        return String.format("%s::%s", formName, formEvent);
    }
    
    public static String getFormAccessEventName() {
        return "_access";
    }
    
    public final String getFormAccessEventKey() {
        return getFormEventKey(getFormName(), getFormAccessEventName());
    }
    
    public static final String getFormAccessEventKey(String formName) {
        return getFormEventKey(formName, getFormAccessEventName());
    }
    
    /**
     * 设置当前的请求 Query 数据，便于前后端交互定数据的可定制化
     */
    public void setContextQueries(List<NameValuePair> queries) {
        CONTEXT_QUERIES.set(queries);
    }
    
    protected NameValuePair[] getContextQueries() {
        List<NameValuePair> queries;
        if ((queries = CONTEXT_QUERIES.get()) == null) {
            return new NameValuePair[0];
        }
        return queries.toArray(new NameValuePair[0]);
    }
}
