package org.socyno.webfwk.state.service;

import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.module.todo.*;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;

import java.util.ArrayList;
import java.util.List;

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
    public static long createTodo(String targetKey, Object targetId, String targetPage, String title, String category,
            Long... assignee) throws Exception {
        return createTodo(targetKey, targetId, null, targetPage, title, category,
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
     * @param title
     *          待办项标题
     * @param category
     *          待办项分类
     * @param assignee
     *          待办项处理人员清单
     */
    public static long createTodoUsePageTmpl(String targetKey, Object targetId, String title, String category,
            long... assignee) throws Exception {
        return createTodo(targetKey, targetId, null, "", title, category, assignee);
    }
    
    /**
     * 创建待办事项。 此方法根据预定义的模板（system.todo.target.page.tmpl）生成流程单的页面，
     * 如果无法生成函数将抛非法参数异常。
     * 
     * @param targetKey
     *          待办项标识
     * @param targetId
     *          流程单编号
     * @param title
     *          待办项标题
     * @param category
     *          待办项分类
     * @param assignee
     *          待办项处理人员清单
     */
    public static long createTodoUsePageTmpl(String targetKey, Object targetId, String title, String category,
            Long... assignee) throws Exception {
        return createTodo(targetKey, targetId, null, "", title, category,
                ConvertUtil.asNonNullUniquePrimitiveLongArray((Object[]) assignee));
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
            return;
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
    public static List<SystemTodoFormDefault> queryOpenedByAssignee(Long assignee) throws Exception {
        return SystemTodoService.getInstance().queryOpenedByAssignee(assignee);
    }
    
    public static long queryOpenedCountByAssignee(Long assignee) throws Exception {
        return SystemTodoService.getInstance().queryOpenedCountByAssignee(assignee);
    }

    public static PagedListWithTotal<SystemTodoFormDefault> queryTodoByCreator(Long createdUserId , Integer page , Integer limit) throws Exception {
        return SystemTodoService.getInstance().queryTodoByCreator(createdUserId , page , limit);
    }

    public static PagedListWithTotal<SystemTodoFormDefault> queryTodoByCloser(Long closedUserId , Integer page , Integer limit) throws Exception {
        return SystemTodoService.getInstance().queryTodoByCloser(closedUserId , page , limit);
    }
    
    public static List<SystemTodoFormDefault> queryTodoWithKeyAndId(String targetKey , Object targetId) throws Exception{
        return SystemTodoService.getInstance().queryOpenedByTargetId(targetKey, CommonUtil.ifNull(targetId, "").toString());
    }
    
    public static List<SystemTodoFormDefault> queryTodoWithCategoryAndId(String category , Object targetId) throws Exception{
        return SystemTodoService.getInstance().queryClosedByCategoryId(category, CommonUtil.ifNull(targetId, "").toString());
    }

    public static SystemTodoFormDetail queryTodoId(Long id) throws Exception{
        return SystemTodoService.getInstance().queryTodoId(id);
    }

}
