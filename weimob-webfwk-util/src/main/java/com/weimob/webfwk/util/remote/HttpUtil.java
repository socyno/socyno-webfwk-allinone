package com.weimob.webfwk.util.remote;

import com.google.gson.JsonElement;
import lombok.NonNull;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

public class HttpUtil {


	public static String concatUrlPath(String prefix, String path) {
		return HttpRequestUtil.concatUrlPath(prefix, path);
	}

	public static String urlEncode(String value) throws UnsupportedEncodingException {
		return HttpRequestUtil.urlEncode(value);
	}

	public static String toQueryString(Map<String, Object> params) {
		return HttpRequestUtil.toQueryString(params);
	}

	public static Map<String, String[]> parseQueryString(String queryString) {
		return HttpRequestUtil.parseQueryString(queryString);
	}

	public static Map<String, Object> fromQueryString(String queryString) {
		return HttpRequestUtil.fromQueryString(queryString);
	}

	public static Object[] fromEnumeration(Enumeration<?> enu) {
		return HttpRequestUtil.fromEnumeration(enu);
	}

	public static HttpUriRequest build(String url, String method, Map<String, Object> params) throws IOException {
		return HttpRequestUtil.getDefault().build(url, method, params);
	}

	public static HttpUriRequest build(String url, String method, byte[] body) throws IOException {
		return HttpRequestUtil.getDefault().build(url, method, body);
	}

	public static HttpUriRequest build(String url, String method, Map<String, Object> params, byte[] body)
			throws IOException {
		return HttpRequestUtil.getDefault().build(url, method, params, body);
	}

	public static HttpUriRequest build(String url, String method, Map<String, Object> params, RequestConfig config)
			throws IOException {
		return HttpRequestUtil.getDefault().build(url, method, params, config);
	}

	public static HttpUriRequest build(String url, String method, Map<String, Object> params,
									   Map<String, Object> headers) throws IOException {
		return HttpRequestUtil.getDefault().build(url, method, params, headers);
	}

	public static CloseableHttpResponse request(String url, String method) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method);
	}

	public static CloseableHttpResponse request(String url, String method, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, timeout);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> params)
			throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> params, int timeout)
			throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, timeout);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> params,
												Map<String, Object> headers) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, headers);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> params,
												Map<String, Object> headers, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, headers, timeout);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> headers, byte[] body)
			throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, headers, body);
	}

	public static CloseableHttpResponse request(String url, String method, Map<String, Object> params,
												Map<String, Object> headers, byte[] body) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, headers, body);
	}

	public static CloseableHttpResponse request(String targetUrl,
												HttpServletRequest request, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().request(targetUrl, request, timeout);
	}

	public static void request(String targetUrl, HttpServletRequest request,
							   HttpServletResponse response, int timeout) throws IOException {
		HttpRequestUtil.getDefault().request(targetUrl, request, response, timeout);
	}

	public static CloseableHttpResponse request(String url, String method,
												Map<String, Object> params, Map<String, Object> headers,
												HttpEntity bodyEntity, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, headers, bodyEntity, timeout);
	}

	public static CloseableHttpResponse request(String url, String method,
												Map<String, Object> params, Map<String, Object> headers,
												byte[] body, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().request(url, method, params, headers, body, timeout);
	}

	public static CloseableHttpResponse request(HttpUriRequest req)
			throws ClientProtocolException, IOException {
		return HttpRequestUtil.getDefault().request(req);
	}

	public static byte[] getResponseData(@NonNull CloseableHttpResponse resp)
			throws IOException {
		return HttpRequestUtil.getResponseData(resp);
	}

	public static JsonElement getResponseJson(CloseableHttpResponse resp)
			throws IOException {
		return HttpRequestUtil.getResponseJson(resp);
	}

	public static JsonElement getResponseJson(CloseableHttpResponse resp,
											  String charset) throws IOException {
		return HttpRequestUtil.getResponseJson(resp, charset);
	}

	public static <T> T getResponseJson(CloseableHttpResponse resp,
										String charset, Class<T> clazz) throws IOException {
		return HttpRequestUtil.getResponseJson(resp, charset, clazz);
	}

	public static String getResponseText(@NonNull CloseableHttpResponse resp)
			throws IOException {
		return HttpRequestUtil.getResponseText(resp);
	}

	public static String getResponseText(@NonNull CloseableHttpResponse resp,
										 String charset) throws IOException {
		return HttpRequestUtil.getResponseText(resp, charset);
	}

	public static int getStatusCode(@NonNull CloseableHttpResponse resp) {
		return HttpRequestUtil.getStatusCode(resp);
	}

	public static CloseableHttpResponse get(String url) throws IOException {
		return HttpRequestUtil.getDefault().get(url);
	}

	public static CloseableHttpResponse get(String url, int timeout)
			throws IOException {
		return HttpRequestUtil.getDefault().get(url, timeout);
	}

	public static CloseableHttpResponse get(String url,
											Map<String, Object> params) throws IOException {
		return HttpRequestUtil.getDefault().get(url, params);
	}

	public static CloseableHttpResponse get(String url,
											Map<String, Object> params, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().get(url, params, timeout);
	}

	public static CloseableHttpResponse postJson(String url, Object data)
			throws IOException {
		return HttpRequestUtil.getDefault().postJson(url, data);
	}

	public static CloseableHttpResponse postJson(String url, Object data,
												 int timeout) throws IOException {
		return HttpRequestUtil.getDefault().postJson(url, data, timeout);
	}

	public static CloseableHttpResponse postJson(String url,
												 Map<String, Object> headers, Object data) throws IOException {
		return HttpRequestUtil.getDefault().postJson(url, headers, data);
	}

	public static CloseableHttpResponse postJson(String url,
												 Map<String, Object> headers, Object data, int timeout)
			throws IOException {
		return HttpRequestUtil.getDefault().postJson(url, headers, data, timeout);
	}

	public static CloseableHttpResponse post(String url) throws IOException {
		return HttpRequestUtil.getDefault().post(url);
	}

	public static CloseableHttpResponse post(String url, int timeout)
			throws IOException {
		return HttpRequestUtil.getDefault().post(url, timeout);
	}

	public static CloseableHttpResponse post(String url,
											 Map<String, Object> params) throws IOException {
		return HttpRequestUtil.getDefault().post(url, params);
	}

	public static CloseableHttpResponse post(String url,
											 Map<String, Object> params, int timeout) throws IOException {
		return HttpRequestUtil.getDefault().post(url, params, timeout);
	}

	public static CloseableHttpResponse post(String url,
											 Map<String, Object> params, Map<String, Object> headers, int timeout)
			throws IOException {
		return HttpRequestUtil.getDefault().post(url, params, headers, timeout);
	}

	public static void close(CloseableHttpResponse resp) {
		HttpRequestUtil.close(resp);
	}

}
