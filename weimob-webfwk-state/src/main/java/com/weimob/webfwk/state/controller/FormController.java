package com.weimob.webfwk.state.controller;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.state.abs.AbstractStateFormInput;
import com.weimob.webfwk.state.service.AttachmentService;
import com.weimob.webfwk.state.service.SimpleLogService;
import com.weimob.webfwk.state.service.StateFormService;
import com.weimob.webfwk.state.util.StateFormDynamicForm;
import com.weimob.webfwk.util.context.HttpMessageConverter;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.remote.HttpRequestUtil;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

public class FormController {
    
    
    @ResponseBody
    @RequestMapping(value = "/logs/{formName}/{formId}", method = RequestMethod.GET)
    public R listLogs(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                               Long fromLogIndex) throws Exception {
        return R.ok().setData(StateFormService.queryLogs(formName, formId, fromLogIndex));
    }
    
    @ResponseBody
    @RequestMapping(value = "/logs/{formName}/{formId}/{detailId}/detail", method = RequestMethod.GET)
    public R getLogDetail(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                @PathVariable("detailId") long detailId) throws Exception {
        return R.ok().setData(SimpleLogService.getLogDetail(detailId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/comments/{formName}/{formId}", method = RequestMethod.GET)
    public R listComments(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            Long fromCommentId) throws Exception {
        return R.ok().setData(StateFormService.queryComments(formName, formId, fromCommentId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/definition/{formName}", method = RequestMethod.GET)
    public R getFormDefinition(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(StateFormService.getFormDefinition(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}", method = RequestMethod.POST)
    public R listForm(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(StateFormService.listForm(formName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotal(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(StateFormService.listFormWithTotal(formName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/{queryName}", method = RequestMethod.POST)
    public R listFormByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(StateFormService.listForm(formName, queryName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/{queryName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotalByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(StateFormService.listFormWithTotal(formName, queryName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}", method = RequestMethod.GET)
    public R getForm(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            HttpServletRequest req) throws Exception {
        StateFormService.setContextQueries(formName, HttpRequestUtil.parseQueryAsPairs(req.getQueryString()));
        try {
            return R.ok().setData(StateFormService.getFormNoActions(formName, formId));
        } finally {
            StateFormService.setContextQueries(formName, null);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/withActions", method = RequestMethod.GET)
    public R getFormWithActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            HttpServletRequest req) throws Exception {
        StateFormService.setContextQueries(formName, HttpRequestUtil.parseQueryAsPairs(req.getQueryString()));
        try {
            return R.ok().setData(StateFormService.getFormWithActions(formName, formId));
        } finally {
            StateFormService.setContextQueries(formName, null);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/actions/simple", method = RequestMethod.GET)
    public R getFormSimpleActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(StateFormService.getFormActionNames(formName, formId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/actions/detail", method = RequestMethod.GET)
    public R getFormDetailActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(StateFormService.getFormActions(formName, formId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/trigger/{formName}/{formAction}", method = RequestMethod.POST)
    public R triggerAction(@PathVariable("formName") String formName, @PathVariable("formAction") String formAction,
            HttpServletRequest req) throws Exception {
        JsonElement jsonData = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"),
                JsonElement.class);
        JsonElement jsonForm;
        if (jsonData == null || !jsonData.isJsonObject() || (jsonForm = ((JsonObject) jsonData).get("form")) == null
                || !jsonForm.isJsonObject()) {
            throw new MessageException("请求数据不可识别.");
        }
        Class<?> actionResult = StateFormService.getActionReturnTypeClass(formName, formAction);
        Class<AbstractStateFormInput> actionClass = StateFormService.getActionFormTypeClass(formName, formAction);
        AbstractStateFormInput formData = HttpMessageConverter.toInstance(actionClass, jsonForm);
        if (formData instanceof StateFormDynamicForm) {
            ((StateFormDynamicForm) formData).setJsonData(jsonForm);
        }
        return R.ok().setData(StateFormService.triggerAction(formName, formAction, formData,
                CommonUtil.getJstring((JsonObject) jsonData, "message"), actionResult));
    }
    
    @ResponseBody
    @RequestMapping(value = "/prepare/{formName}/{formId}/{formAction}", method = RequestMethod.GET)
    public R triggerPrepare(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            @PathVariable("formAction") String formAction, HttpServletRequest req) throws Exception {
        List<NameValuePair> queries;
        if ((queries = URLEncodedUtils.parse(StringUtils.trimToEmpty(req.getQueryString()),
                Charset.forName("UTF-8"))) == null) {
            queries = Collections.emptyList();
        }
        return R.ok().setData(StateFormService.triggerPrepare(formName, formAction, formId,
                queries.toArray(new NameValuePair[0])));
    }
    
    @ResponseBody
    @RequestMapping(value = "/create/{formName}/{formAction}", method = RequestMethod.POST)
    public R triggerSubmitAction(@PathVariable("formName") String formName,
            @PathVariable("formAction") String formAction, HttpServletRequest req) throws Exception {
        String bodyJson = new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8");
        Class<AbstractStateFormInput> actionClass = StateFormService.getActionFormTypeClass(formName, formAction);
        return R.ok().setData(StateFormService.triggerCreateAction(formName, formAction,
                HttpMessageConverter.toInstance(actionClass, bodyJson)));
    }
    
    @ResponseBody
    @RequestMapping(value = "/upload/{formName}", method = RequestMethod.POST)
    public R uploadCreate(@PathVariable("formName") String formName, MultipartHttpServletRequest req) throws Exception {
        if (!StateFormService.checkFormDefined(formName)) {
            throw new MessageException(String.format("给定的表单（%s）未注册", formName));
        }
        return R.ok().setData(AttachmentService.formUpload(formName, req));
    }
    
    @RequestMapping(value = "/upload/{formName}/{formId}/{attachementId}/download", method = RequestMethod.POST)
    public void uploadDownload(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                @PathVariable("attachementId") long attachementId, String preview,
                                HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (CommonUtil.parseBoolean(preview)) {
            AttachmentService.preview(attachementId, req, resp);
            return;
        }
        AttachmentService.download(attachementId, req, resp);
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options", method = RequestMethod.GET)
    public R queryTypeFieldOptions(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String keyword, Long formId) throws Exception {
        return R.ok().setData(StateFormService.queryFieldTypeOptions(fieldTypeKey, keyword, formName, formId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options/withQuery", method = RequestMethod.POST)
    public R queryTypeFieldOptionsWithQuery(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, HttpServletRequest req) throws Exception {
        String bodyJson = new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8");
        return R.ok().setData(StateFormService.queryFieldTypeOptions(formName, fieldTypeKey, bodyJson));
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/values", method = RequestMethod.GET)
    public R queryTypeFieldValues(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String[] values) throws Exception {
        return R.ok().setData(StateFormService.queryFieldTypeValues(fieldTypeKey, values));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/construction", method = RequestMethod.GET)
    public R queryTypeFormConstruction(@PathVariable("formTypeKey") String formTypeKey) throws Exception {
        return R.ok().setData(StateFormService.queryFormTypeDefinition(formTypeKey));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs", method = RequestMethod.GET)
    public R queryTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey) throws Exception {
        return R.ok().setData(StateFormService.getFieldCustomDefinition(formTypeKey));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs/update", method = RequestMethod.POST)
    public R updateTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey, Long revision,
            @RequestBody List<Map<String, String>> defintion) throws Exception {
        return R.ok().setData(
                StateFormService.saveFieldCustomDefinition(formTypeKey, CommonUtil.toJson(defintion), revision));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs/preview", method = RequestMethod.POST)
    public R previewTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey, 
                @RequestBody List<Map<String, String>> defintion) throws Exception {
        return R.ok().setData(StateFormService.previewFieldCustomDefinition(formTypeKey, 
                                    CommonUtil.toJson(defintion)));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews", method = RequestMethod.GET)
    public R queryFormExtraViews(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(StateFormService.queryFormExtraViews(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews/update", method = RequestMethod.POST)
    public R saveFormExtraViews(@PathVariable("formName") String formName, 
                @RequestBody List<String> extraViews) throws Exception {
        StateFormService.saveFormExtraViews(formName, extraViews);
        return R.ok();
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews/definition", method = RequestMethod.GET)
    public R queryFormExtraViewDefinitions(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(StateFormService.queryFormExtraViewDefinitions(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/flowchart/definition", method = RequestMethod.GET)
    public R getFormFlowDefinition(@PathVariable("formName") String formName, String unchanged, String formId)
            throws Exception {
        Long formLongId = CommonUtil.parseLong(formId, 0L);
        return R.ok().setData(StateFormService.parseFormFlowDefinition(formName,
                CommonUtil.parseBoolean(unchanged), formLongId <= 0 ? null : formLongId));
    }
}
