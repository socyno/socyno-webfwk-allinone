package org.socyno.webfwk.module.basic;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.basic.DynamicStateForm;
import org.socyno.webfwk.state.service.CommonAttachmentService;
import org.socyno.webfwk.state.service.CommonStateFormService;
import org.socyno.webfwk.state.service.SimpleLogService;
import org.socyno.webfwk.util.context.HttpMessageConverter;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.remote.HttpRequestUtil;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FormController {
    
    
    @ResponseBody
    @RequestMapping(value = "/logs/{formName}/{formId}", method = RequestMethod.GET)
    public R listLogs(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                               Long fromLogIndex) throws Exception {
        return R.ok().setData(CommonStateFormService.queryLogs(formName, formId, fromLogIndex));
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
        return R.ok().setData(CommonStateFormService.queryComments(formName, formId, fromCommentId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/definition/{formName}", method = RequestMethod.GET)
    public R getFormDefinition(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(CommonStateFormService.getFormDefinition(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}", method = RequestMethod.POST)
    public R listForm(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(CommonStateFormService.ListForm(formName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotal(@PathVariable("formName") String formName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(CommonStateFormService.ListFormWithTotal(formName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/{queryName}", method = RequestMethod.POST)
    public R listFormByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(CommonStateFormService.ListForm(formName, queryName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/list/{formName}/{queryName}/withTotal", method = RequestMethod.POST)
    public R listFormWithTotalByName(@PathVariable("formName") String formName, @PathVariable("queryName") String queryName, HttpServletRequest req) throws Exception {
        JsonElement data = CommonUtil.fromJson(new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8"), JsonElement.class);
        return R.ok().setData(CommonStateFormService.ListFormWithTotal(formName, queryName, data));
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}", method = RequestMethod.GET)
    public R getForm(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            HttpServletRequest req) throws Exception {
        CommonStateFormService.setContextQueries(formName, HttpRequestUtil.parseQueryAsPairs(req.getQueryString()));
        try {
            return R.ok().setData(CommonStateFormService.getFormNoActions(formName, formId));
        } finally {
            CommonStateFormService.setContextQueries(formName, null);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/withActions", method = RequestMethod.GET)
    public R getFormWithActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
            HttpServletRequest req) throws Exception {
        CommonStateFormService.setContextQueries(formName, HttpRequestUtil.parseQueryAsPairs(req.getQueryString()));
        try {
            return R.ok().setData(CommonStateFormService.getFormWithActions(formName, formId));
        } finally {
            CommonStateFormService.setContextQueries(formName, null);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/actions/simple", method = RequestMethod.GET)
    public R getFormSimpleActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(CommonStateFormService.getFormActionNames(formName, formId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/get/{formName}/{formId}/actions/detail", method = RequestMethod.GET)
    public R getFormDetailActions(@PathVariable("formName") String formName, @PathVariable("formId") long formId)
            throws Exception {
        return R.ok().setData(CommonStateFormService.getFormActions(formName, formId));
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
        Class<?> actionResult = CommonStateFormService.getActionReturnTypeClass(formName, formAction);
        Class<AbstractStateForm> actionClass = CommonStateFormService.getActionFormTypeClass(formName, formAction);
        AbstractStateForm formData = HttpMessageConverter.toInstance(actionClass, jsonForm);
        if (formData instanceof DynamicStateForm) {
            ((DynamicStateForm) formData).setJsonData(jsonForm);
        }
        return R.ok().setData(CommonStateFormService.triggerAction(formName, formAction, formData,
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
        return R.ok().setData(CommonStateFormService.triggerPrepare(formName, formAction, formId,
                queries.toArray(new NameValuePair[0])));
    }
    
    @ResponseBody
    @RequestMapping(value = "/create/{formName}/{formAction}", method = RequestMethod.POST)
    public R triggerSubmitAction(@PathVariable("formName") String formName,
            @PathVariable("formAction") String formAction, HttpServletRequest req) throws Exception {
        String bodyJson = new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8");
        Class<AbstractStateForm> actionClass = CommonStateFormService.getActionFormTypeClass(formName, formAction);
        return R.ok().setData(CommonStateFormService.triggerSubmitAction(formName, formAction,
                HttpMessageConverter.toInstance(actionClass, bodyJson)));
    }
    
    @ResponseBody
    @RequestMapping(value = "/upload/{formName}", method = RequestMethod.POST)
    public R uploadCreate(@PathVariable("formName") String formName, MultipartHttpServletRequest req) throws Exception {
        if (!CommonStateFormService.checkFormDefined(formName)) {
            throw new MessageException(String.format("给定的表单（%s）未注册", formName));
        }
        return R.ok().setData(CommonAttachmentService.formUpload(formName, req));
    }
    
    @RequestMapping(value = "/upload/{formName}/{formId}/{attachementId}/download", method = RequestMethod.POST)
    public void uploadDownload(@PathVariable("formName") String formName, @PathVariable("formId") long formId,
                                @PathVariable("attachementId") long attachementId, String preview,
                                HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (CommonUtil.parseBoolean(preview)) {
            CommonAttachmentService.preview(attachementId, req, resp);
            return;
        }
        CommonAttachmentService.download(attachementId, req, resp);
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options", method = RequestMethod.GET)
    public R queryTypeFieldOptions(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String keyword, Long formId) throws Exception {
        return R.ok().setData(CommonStateFormService.queryFieldTypeOptions(fieldTypeKey, keyword, formName, formId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/options/withQuery", method = RequestMethod.POST)
    public R queryTypeFieldOptionsWithQuery(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, HttpServletRequest req) throws Exception {
        String bodyJson = new String(IOUtils.toByteArray(req.getInputStream()), "UTF-8");
        return R.ok().setData(CommonStateFormService.queryFieldTypeOptions(formName, fieldTypeKey, bodyJson));
    }
    
    @ResponseBody
    @RequestMapping(value = "/field/{formName}/{fieldTypeKey}/values", method = RequestMethod.GET)
    public R queryTypeFieldValues(@PathVariable("formName") String formName,
            @PathVariable("fieldTypeKey") String fieldTypeKey, String[] values) throws Exception {
        return R.ok().setData(CommonStateFormService.queryFieldTypeValues(fieldTypeKey, values));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/construction", method = RequestMethod.GET)
    public R queryTypeFormConstruction(@PathVariable("formTypeKey") String formTypeKey) throws Exception {
        return R.ok().setData(CommonStateFormService.queryFormTypeDefinition(formTypeKey));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs", method = RequestMethod.GET)
    public R queryTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey) throws Exception {
        return R.ok().setData(CommonStateFormService.getFieldCustomDefinition(formTypeKey));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs/update", method = RequestMethod.POST)
    public R updateTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey, Long revision,
            @RequestBody List<Map<String, String>> defintion) throws Exception {
        return R.ok().setData(
                CommonStateFormService.saveFieldCustomDefinition(formTypeKey, CommonUtil.toJson(defintion), revision));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formTypeKey}/viewattrs/preview", method = RequestMethod.POST)
    public R previewTypeFormViewAttrs(@PathVariable("formTypeKey") String formTypeKey, 
                @RequestBody List<Map<String, String>> defintion) throws Exception {
        return R.ok().setData(CommonStateFormService.previewFieldCustomDefinition(formTypeKey, 
                                    CommonUtil.toJson(defintion)));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews", method = RequestMethod.GET)
    public R queryFormExtraViews(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(CommonStateFormService.queryFormExtraViews(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews/update", method = RequestMethod.POST)
    public R saveFormExtraViews(@PathVariable("formName") String formName, 
                @RequestBody List<String> extraViews) throws Exception {
        CommonStateFormService.saveFormExtraViews(formName, extraViews);
        return R.ok();
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/extraviews/definition", method = RequestMethod.GET)
    public R queryFormExtraViewDefinitions(@PathVariable("formName") String formName) throws Exception {
        return R.ok().setData(CommonStateFormService.queryFormExtraViewDefinitions(formName));
    }
    
    @ResponseBody
    @RequestMapping(value = "/form/{formName}/flowchart/definition", method = RequestMethod.GET)
    public R getFormFlowDefinition(@PathVariable("formName") String formName, String unchanged, String formId)
            throws Exception {
        Long formLongId = CommonUtil.parseLong(formId, 0L);
        return R.ok().setData(CommonStateFormService.parseFormFlowDefinition(formName,
                CommonUtil.parseBoolean(unchanged), formLongId <= 0 ? null : formLongId));
    }
}
