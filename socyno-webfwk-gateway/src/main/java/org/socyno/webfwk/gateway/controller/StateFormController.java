package org.socyno.webfwk.gateway.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.gateway.util.HttpRedirectUtil;
import org.socyno.webfwk.gateway.util.HttpRedirectUtil.ServiceBackend;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.service.StateFormService;
import org.socyno.webfwk.state.service.StateFormService.StateFormRegister;
import org.socyno.webfwk.util.remote.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.reinert.jjschema.Attributes;

@RestController
@RequestMapping(value="/form")
public class StateFormController {
    
    private static void redirectToBackend(ServiceBackend backend, String targetUrl, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(backend, targetUrl, req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取流程单的操作记录")
    @RequestMapping(value = "/logs/{formName}/{formId}", method = RequestMethod.GET)
    public void listLogs(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                               Long fromLogIndex, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/logs/%s/%s", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取可用的流程单指定操作的前后明细")
    @RequestMapping(value = "/logs/{formName}/{formId}/{detailId}/detail", method = RequestMethod.GET)
    public void getLogDetail(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                @PathVariable("detailId") long detailId,
                                HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/logs/%s/%s/%s/detail", formName, formId, detailId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取流程单的备注（讨论）列表")
    @RequestMapping(value = "/comments/{formName}/{formId}", method = RequestMethod.GET)
    public void listComments(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                Long fromCommentId, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/comments/%s/%s", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取流程单的定义")
    @RequestMapping(value = "/definition/{formName}", method = RequestMethod.GET)
    public void getFormDefinition(@PathVariable("formName") String formName, 
            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/definition/%s", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单列表（使用默认查询）")
    @RequestMapping(value = "/list/{formName}", method = RequestMethod.POST)
    public void listForm(@PathVariable("formName") String formName,
                            @RequestBody Map<String, Object> form, 
                            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/list/%s", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单列表（使用指定查询）")
    @RequestMapping(value = "/list/{formName}/{queryName}", method = RequestMethod.POST)
    public void listFormByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName,
                            @RequestBody Map<String, Object> form, 
                            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/list/%s/%s", formName, queryName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单列表（使用默认查询，同时提供数据集总条数）")
    @RequestMapping(value = "/list/{formName}/withTotal", method = RequestMethod.POST)
    public void listFormWithTotal(@PathVariable("formName") String formName, 
                            @RequestBody Map<String, Object> form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/list/%s/withTotal", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单列表（使用指定查询，同时提供数据集总条数）")
    @RequestMapping(value = "/list/{formName}/{queryName}/withTotal", method = RequestMethod.POST)
    public void listFormByNameWithTotal(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName,
                            @RequestBody Map<String, Object> form, 
                            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/list/%s/%s/withTotal", formName, queryName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单详情")
    @RequestMapping(value = "/get/{formName}/{formId}", method = RequestMethod.GET)
    public void getForm(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/get/%s/%s", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询流程单详情(返回当前可执行操作)")
    @RequestMapping(value = "/get/{formName}/{formId}/withActions", method = RequestMethod.GET)
    public void getFormWithActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                    HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/get/%s/%s/withActions", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询给定表单的可执行操作详情")
    @RequestMapping(value = "/get/{formName}/{formId}/actions/detail", method = RequestMethod.GET)
    public void getFormActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                    HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/get/%s/%s/actions/detail", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "查询给定表单的可执行操作名称")
    @RequestMapping(value = "/get/{formName}/{formId}/actions/simple", method = RequestMethod.GET)
    public void getFormActionNames(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                    HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/get/%s/%s/actions/simple", formName, formId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "执行流程单事件(响应数据参照流程单的定义)")
    @RequestMapping(value = "/trigger/{formName}/{formAction}", method = RequestMethod.POST)
    public void triggerAction(@PathVariable("formName") String formName, @PathVariable("formAction") String formAction,
                        @RequestBody BasicStateForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/trigger/%s/%s", formName, formAction), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取流程表单特定事件的执行前准备数据")
    @RequestMapping(value = "/prepare/{formName}/{formId}/{formAction}", method = RequestMethod.GET)
    public void triggerPrepare(@PathVariable("formName") String formName,
                        @PathVariable("formAction") String formAction,  @PathVariable("formId") String formId, 
                        HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/prepare/%s/%s/%s", formName, formId, formAction), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "执行表单创建事件")
    @RequestMapping(value = "/create/{formName}/{formAction}", method = RequestMethod.POST)
    public void triggerSubmitAction(@PathVariable("formName") String formName, @PathVariable("formAction") String formAction, 
                        @RequestBody BasicStateForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/create/%s/%s", formName, formAction), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "List:新建流程单时，上传附件(可返回附件的清单)")
    @RequestMapping(value = "/upload/{formName}", method = RequestMethod.POST)
    public void uploadCreate(@PathVariable("formName") String formName,  HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/upload/%s", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "下载流程单中的附件")
    @RequestMapping(value = "/upload/{formName}/{formId}/{attachementId}/download", method = RequestMethod.POST)
    public void uploadDownload(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                @PathVariable("attachementId") long attachementId, String preview,
                                HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/upload/%s/%s/%s/download", formName, formId, attachementId), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取预定义字段类型的选项列表。提供检索关键字（keyworkd）可获取全部有效列表, 如果在关键字内容为空时返回空列表，意味着必须使用关键字检索。")
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options", method = RequestMethod.GET)
    public void queryTypeFieldOptions(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String keyword, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/field/%s/%s/options", formName, fieldTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取预定义字段类型的选项列表。 查询条件，请参见对应的字段定义信息。")
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options/withQuery", method = RequestMethod.POST)
    public void queryTypeFieldOptionsWithQuery(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/field/%s/%s/options/withQuery", formName, fieldTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取显示/编辑界面的排版定义数据")
    @RequestMapping(value = "/form/{formName}/{formTypeKey}/viewattrs", method = RequestMethod.GET)
    public void queryTypeFormViewAttrs(@PathVariable("formName") String formName, 
                                @PathVariable("formTypeKey") String formTypeKey,
                                HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/form/%s/viewattrs", formTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "更新显示/编辑界面的排版定义数据")
    @RequestMapping(value = "/form/{formName}/{formTypeKey}/viewattrs/update", method = RequestMethod.POST)
    public void updateTypeFormViewAttrs(@PathVariable("formName") String formName, 
                                @PathVariable("formTypeKey") String formTypeKey, 
                                @RequestBody List<Map<String, String>> defintion,
                                HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/form/%s/viewattrs/update", formTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "检查或预览显示/编辑界面的排版定义数据")
    @RequestMapping(value = "/form/{formName}/{formTypeKey}/viewattrs/preview", method = RequestMethod.POST)
    public void previewTypeFormViewAttrs(@PathVariable("formName") String formName, 
                                @PathVariable("formTypeKey") String formTypeKey, 
                                @RequestBody List<Map<String, String>> defintion,
                                HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName), String.format("/api/form/form/%s/viewattrs/preview", formTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取指定的关联视图界面模型数据")
    @RequestMapping(value = "/form/{formName}/{formTypeKey}/construction", method = RequestMethod.GET)
    public void queryFormExtraConstruction(@PathVariable("formName") String formName,
            @PathVariable("formTypeKey") String formTypeKey, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/form/%s/construction", formTypeKey), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取额外的关联视图名称清单")
    @RequestMapping(value = "/form/{formName}/extraviews", method = RequestMethod.GET)
    public void queryFormExtraViews(@PathVariable("formName") String formName, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/form/%s/extraviews", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "更新额外的关联视图名称清单")
    @RequestMapping(value = "/form/{formName}/extraviews/update", method = RequestMethod.POST)
    public void saveFormExtraViews(@PathVariable("formName") String formName, @RequestBody List<String> extraViews,
            HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/form/%s/extraviews/update", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取额外的关联视图模型清单")
    @RequestMapping(value = "/form/{formName}/extraviews/definition", method = RequestMethod.GET)
    public void queryFormExtraViewDefinitionss(@PathVariable("formName") String formName, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),
                String.format("/api/form/form/%s/extraviews/definition", formName), req, res);
    }
    
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取表单流程图定义数据")
    @RequestMapping(value = "/form/{formName}/flowchart/definition", method = RequestMethod.GET)
    public void getFlowChartDefinition(@PathVariable("formName") String formName, HttpServletRequest req, HttpServletResponse res) throws Exception {
        redirectToBackend(ServiceBackend.getForm(formName),String.format("/api/form/form/%s/flowchart/definition", formName), req, res);
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "获取已注册通用流程单列表")
    @RequestMapping(value = "/form/list", method = RequestMethod.GET)
    public R listDefinedForm() throws Exception {
        return R.ok().setData(StateFormService.listStateFormRegister());
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "注册通用流程单")
    @RequestMapping(value = "/form/add", method = RequestMethod.POST)
    public R addDefinedForm(@RequestBody StateFormRegister form) throws Exception {
        StateFormService.registerForm(form);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "编辑注册的流程单")
    @RequestMapping(value = "/form/update", method = RequestMethod.POST)
    public R updateDefinedForm(@RequestBody StateFormRegister form) throws Exception {
        StateFormService.updateForm(form);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "删除注册的流程单")
    @RequestMapping(value = "/form/delete/{formName}", method = RequestMethod.POST)
    public R removeDefinedForm(@PathVariable("formName") String formName) throws Exception {
        StateFormService.removeForm(formName);
        return R.ok();
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @Attributes(title = "禁用/启动通用流程单")
    @RequestMapping(value = "/form/toggle/{formName}", method = RequestMethod.POST)
    public R toggleForm(@PathVariable("formName") String formName) throws Exception {
        StateFormService.toggleForm(formName);
        return R.ok();
    }
}
