package com.weimob.webfwk.state.service;

import com.weimob.webfwk.state.field.OptionSystemUser;
import com.weimob.webfwk.state.module.todo.*;
import com.weimob.webfwk.state.module.user.SystemUserService;
import com.weimob.webfwk.state.module.user.WindowsAdService;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.AbstractUser;
import com.weimob.webfwk.util.model.PagedListWithTotal;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.ConvertUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SimpleTodoService {
    
    /**
     * 创建待办事项清单
     * 
     * @param targetKey
     *                   待办项标识
     * @param targetId
     *                   流程单编号
     * @param applierId
     *                   流程单发起人
     * @param targetPage
     *                   待办项页面
     * @param title
     *                   待办项标题
     * @param category
     *                   待办项分类
     * @param assignee
     *                   待办项处理人员清单
     */
    public static long createTodo(String targetKey, Object targetId, Long applierId, String targetPage, String title,
            String category, long... assignee) throws Exception {
        SystemTodoFormCreation form = new SystemTodoFormCreation();
        form.setTitle(title);
        form.setCategory(category);
        form.setTargetKey(targetKey);
        form.setTargetId(CommonUtil.ifNull(targetId, "").toString());
        form.setTargetPage(targetPage);
        if (applierId != null) {
            form.setApplyUser(new OptionSystemUser().setId(applierId));
        }
        if (assignee != null && assignee.length > 0) {
            List<OptionSystemUser> options = new ArrayList<>();
            for (long userId : assignee) {
                options.add(new OptionSystemUser().setId(userId));
            }
            form.setAssignees(options);
        }
        return SystemTodoService.getInstance().triggerCreateAction(SystemTodoService.EVENTS.Create.getName(), form)
                .getId();
    }
    
    /**
     * 创建待办事项清单
     * 
     * @param targetKey
     *          待办项标识
     * @param targetId
     *          流程单编号
     * @param targetPage
     *          待办项页面
     * @param title
     *          待办项标题
     * @param category
     *          待办项分类
     * @param assignee
     *          待办项处理人员清单
     */
    public static long createTodo(String targetKey, Object targetId, Long applier, String targetPage, String title, String category,
            Long... assignee) throws Exception {
        return createTodo(targetKey, targetId, applier, targetPage, title, category,
                ConvertUtil.asNonNullUniquePrimitiveLongArray((Object[]) assignee));
    }
    
    /**
     * 创建待办事项。 此方法根据预定义的模板（system.todo.target.page.tmpl）生成流程单的页面，
     * 如果无法生成函数将抛非法参数异常。
     * 
     * @param targetKey
     *          待办项标识
     * @param targetId
     *          流程单编号
     * @param applier
     *          流程单发起人
     * @param title
     *          待办项标题
     * @param category
     *          待办项分类
     * @param assignee
     *          待办项处理人员清单
     */
    public static long createTodoUsePageTmpl(String targetKey, Object targetId, long applier, String title, String category,
            Long... assignee) throws Exception {
        return createTodo(targetKey, targetId, applier, "", title, category, assignee);
    }
    
    /**
     * 创建待办事项。 此方法根据预定义的模板（system.todo.target.page.tmpl）生成流程单的页面，
     * 如果无法生成函数将抛非法参数异常。
     * 
     * @param targetKey
     *          待办项标识
     * @param targetId
     *          流程单编号
     * @param applierCode
     *          流程单发起人账户
     * @param title
     *          待办项标题
     * @param category
     *          待办项分类
     * @param assigneeCodes
     *          待办项处理人员账户清单
     */
    public static long createTodoUsePageTmpl(String targetKey, Object targetId, String applierCode, String title,
            String category, String... assigneeCodes) throws Exception {
        AbstractUser applierEntity;
        Set<Long> assigneeIds = new HashSet<>();
        for (String username : assigneeCodes) {
            if (StringUtils.isBlank(username)) {
                continue;
            }
            applierEntity = SystemUserService.getInstance().forceCreateUser(
                    new WindowsAdService.SystemWindowsAdUser().setLogin(username).setName(username));
            assigneeIds.add(applierEntity.getId());
        }
        applierEntity = SystemUserService.getInstance().forceCreateUser(
                new WindowsAdService.SystemWindowsAdUser().setLogin(applierCode).setName(applierCode));
        return createTodoUsePageTmpl(targetKey, targetId, applierEntity.getId(), title, category, assigneeIds.toArray(new Long[0]));
    }
    
    /**
     * 关闭待办事项
     * @param targetKey
     *          流程单名称
     * @param targetId
     *          流程编号
     * @param result
     *          处理结果
     */
    public static void closeTodo(String targetKey, Object targetId, String result) throws Exception {
        List<SystemTodoFormDefault> list;
        if ((list = SystemTodoService.getInstance().queryOpenedByTargetId(targetKey,
                CommonUtil.ifNull(targetId, "").toString())) == null || list.size() <= 0) {
            throw new MessageException(
                    String.format("提供的待办事项不存在，或已经关闭(targetKey=%s, targetId=%s)", targetKey, targetId));
        }
        SystemTodoFormClose form = new SystemTodoFormClose();
        form.setId(list.get(0).getId());
        form.setRevision(list.get(0).getRevision());
        form.setResult(result);
        SystemTodoService.getInstance().triggerAction(SystemTodoService.EVENTS.Close.getName(), form);
    }
    
    /**
     * 查询指定用户待处理的待办事项
     * 
     * @param assignee 用户标识
     */
    public static List<SystemTodoFormDetail> queryOpenedByAssignee(Long assignee) throws Exception {
        return SystemTodoService.getInstance().queryOpenedByAssignee(assignee);
    }
    
    public static long queryOpenedCountByAssignee(Long assignee) throws Exception {
        return SystemTodoService.getInstance().queryOpenedCountByAssignee(assignee);
    }
    
    public static PagedListWithTotal<SystemTodoFormDetail> queryTodoByApplier(Long createdUserId , Integer page , Integer limit) throws Exception {
        return SystemTodoService.getInstance().queryTodoByApplier(createdUserId , page , limit);
    }
    
    public static PagedListWithTotal<SystemTodoFormDetail> queryTodoByCloser(Long closedUserId , Integer page , Integer limit) throws Exception {
        return SystemTodoService.getInstance().queryTodoByCloser(closedUserId , page , limit);
    }
}
