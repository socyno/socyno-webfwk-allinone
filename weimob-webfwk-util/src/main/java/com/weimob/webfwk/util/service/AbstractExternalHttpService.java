package com.weimob.webfwk.util.service;

import com.google.gson.JsonElement;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.nio.charset.Charset;
import java.util.Map;

public abstract class AbstractExternalHttpService {
    
    protected int getRequestTimeoutMS() {
        return 30000;
    }
    
    protected Charset getCharset() {
        return Charset.forName("UTF-8");
    }
    
    protected String getContentTypeJson() {
        return String.format("application/json;charset=%s", getCharset().name().toLowerCase());
    }
    
    protected String getContentTypeForm() {
        return String.format("application/x-www-form-urlencoded;charset=%s", getCharset().name().toLowerCase());
    }
    
    public abstract String getRemoteUrl();
    
    public String getApiFullUrl(String path) {
        return HttpUtil.concatUrlPath(getRemoteUrl(), path);
    }
    
    public JsonElement postJson(String path, Object body)
            throws Exception {
        return postJson(path, body, null, null);
    }
    
    public JsonElement postJson(String path, Map<String, Object> params)
            throws Exception {
        return postJson(path, null, params, null);
    }
    
    public JsonElement postJson(String path, Object body, Map<String, Object> headers)
            throws Exception {
        return postJson(path, body, null, headers);
    }
    
    public JsonElement postJson(String path, Map<String, Object> params, Map<String, Object> headers)
            throws Exception {
        return postJson(path, null, params, headers);
    }
    
    public JsonElement postJson(String path, Object body, Map<String, Object> params, Map<String, Object> headers)
            throws Exception {
        return requestJson(path, "POST", body, params, headers);
    }
    
    public JsonElement getJson(String path) throws Exception {
        return getJson(path, null, null);
    }
    
    public JsonElement getJson(String path, Map<String, Object> params) throws Exception {
        return getJson(path, params, null);
    }
    
    public JsonElement getJson(String path, Map<String, Object> params, Map<String, Object> headers) throws Exception {
        return requestJson(path, "GET", null, params, headers);
    }
    
    public JsonElement requestJson(String path, String method, Object body, Map<String, Object> params, 
                Map<String, Object> headers) throws Exception {
        String respText = null;
        CloseableHttpResponse response = null;
        try {
            path = StringUtils.trimToEmpty(path);
            String urlPreffix = getRemoteUrl();
            Charset charset = getCharset();
            method = StringUtils.trimToEmpty(method).toUpperCase();
            if (body != null && !(body instanceof byte[])) {
                body = CommonUtil.toJson(body).getBytes(getCharset());
            }
            ObjectMap headerx = new ObjectMap().put("Content-Type", body != null
                    ? getContentTypeJson() : getContentTypeForm());
            if (headers != null) {
                headerx.addAll(headers);
            }
            response = HttpUtil.request(HttpUtil.concatUrlPath(urlPreffix, path), method, params, headerx.asMap(),
                            (byte[]) body, getRequestTimeoutMS());
            respText = HttpUtil.getResponseText(response, charset.name());
            return CommonUtil.fromJson(respText, JsonElement.class);
        } finally {
            HttpUtil.close(response);
        }
    }

}
