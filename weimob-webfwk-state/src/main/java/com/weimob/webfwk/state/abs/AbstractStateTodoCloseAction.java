package com.weimob.webfwk.state.abs;

import com.weimob.webfwk.state.exec.StateFormNoTodoClosedEventFoundException;
import com.weimob.webfwk.state.service.SimpleTodoService;
import com.weimob.webfwk.util.tool.StringUtils;

/**
 * 代办事项关闭事件.作为内部事件的一种, Leave 事件的子类, 为代办事项专用
 */
public abstract class AbstractStateTodoCloseAction<S extends AbstractStateFormBase>
        extends AbstractStateLeaveAction<S> {
    /**
     * 构造器
     * 
     * @param display 事件的显示名称
     * @param targetState 触发关闭代办事项的状态
     */
    public AbstractStateTodoCloseAction(String display, String targetState) {
        super(display, targetState);
    }
    
    /**
     * 构建代办事项关闭的原因说明，如果未提供(返回空白)，则不关闭此代办事项。
     */
    protected String getClosedTodoReason(String event, S originForm, AbstractStateFormInput form) throws Exception {
        StringBuilder reason = new StringBuilder(
                getContextFormService().getExternalFormAction(getContextFormEvent()).getDisplay());
        if (StringUtils.isNotBlank(getContextFormEventMessage())) {
            reason.append(":").append(getContextFormEventMessage());
        }
        return reason.toString();
    }
    
    protected abstract String getClosedTodoEvent(String event, S originForm, AbstractStateFormInput form) throws Exception;
    
    @SuppressWarnings("unchecked")
    protected final String getClosedTodoTargetKey(String event, S originForm, AbstractStateFormInput form) throws Exception {
        String createdEvent = getClosedTodoEvent(event, originForm, form);
        AbstractStateAction<S, ?, ?> created = getContextFormService().getInternalFormAction(createdEvent);
        if (created == null || !(created instanceof AbstractStateTodoCreateAction)) {
            throw new StateFormNoTodoClosedEventFoundException(getContextFormService().getFormName(), form.getId(), event);
        }
        return ((AbstractStateTodoCreateAction<S>)created).getTodoTargetKey(createdEvent, originForm, form);
    }
    
    public final Void handle(String event, S originForm, AbstractStateFormInput form, String message) throws Exception {
        String closeTodoReason = getClosedTodoReason(event, originForm, form);
        if (StringUtils.isBlank(closeTodoReason = getClosedTodoReason(event, originForm, form))) {
            return null;
        }
        String closeTodoTargetKey = getClosedTodoTargetKey(event, originForm, form);
        SimpleTodoService.closeTodo(closeTodoTargetKey, form.getId(), closeTodoReason);
        return null;
    }
}
