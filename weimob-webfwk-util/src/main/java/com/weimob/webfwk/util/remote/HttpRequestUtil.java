package com.weimob.webfwk.util.remote;

import com.google.gson.JsonElement;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class HttpRequestUtil {

	@Getter
	private final static HttpRequestUtil Default =  new HttpRequestUtil();

	private final static int DEFAULT_TIMEOUT_MS = 20000;
	private final static int DEFAULT_MAX_LOG_BODY = 1024 * 1204;
	private final static byte[] LOG_RESPONSED_DATA_SKIPPED = "--SKIPPED--"
			.getBytes();

	public static String concatUrlPath(String prefix, String path) {
        return String.format("%s/%s", StringUtils.trimToEmpty(prefix).replaceAll("[/\\s]+$", ""),
                StringUtils.trimToEmpty(path).replaceAll("^[/\\s]+", ""));
    }
    
    public static String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
    
	public static String toQueryString(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		RequestBuilder builder = RequestBuilder.create("GET").setUri("");
		for (Map.Entry<String, Object> p : params.entrySet()) {
			String key = p.getKey();
			Object val = p.getValue();
			if (val instanceof Object[]) {
				for (Object v : (Object[]) val) {
					if (v == null) {
						continue;
					}
					builder.addParameter(key, v.toString());
				}
			} else if (val instanceof Collection) {
				for (Object v : (Collection<?>) val) {
					if (v == null) {
						continue;
					}
					builder.addParameter(key, v.toString());
				}
			} else if (val != null) {
				builder.addParameter(key, val.toString());
			}
		}
		return builder.build().getURI().getRawQuery();
	}
	
    public static Map<String, String[]> parseQueryString(String queryString) {
        Map<String, Object> queries;
        if ((queries = fromQueryString(queryString)) == null) {
            return Collections.emptyMap();
        }
        Map<String, String[]> params = new HashMap<>();
        for (Map.Entry<String, Object> q : queries.entrySet()) {
            Object value;
            if ((value = q.getValue()) == null) {
                continue;
            }
            String[] paramValues;
            if (value.getClass().isArray()) {
                Object paramValue;
                paramValues = new String[((Object[]) value).length];
                for (int i = 0; i < paramValues.length; i++) {
                    paramValue = ((Object[]) value)[i];
                    paramValues[i] = (String) CommonUtil.ifNull(paramValue, "", paramValue.toString());
                }
            } else if (Collection.class.isAssignableFrom(value.getClass())) {
                int i = 0;
                paramValues = new String[((Collection<?>) value).size()];
                for (Object v : (Collection<?>) value) {
                    paramValues[i++] = (String) CommonUtil.ifNull(v, "", v.toString());
                }
            } else {
                paramValues = new String[] { value.toString() };
            }
            params.put(q.getKey(), paramValues);
        }
        return params;
    }
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> fromQueryString(String queryString) {
		Map<String, Object> params = new HashMap<>();
		if (StringUtils.isBlank(queryString)) {
			return params;
		}
		List<NameValuePair> queries = null;
		if ((queries = URLEncodedUtils.parse(queryString,
				Charset.forName("UTF-8"))) == null
				|| queries.size() <= 0) {
			return params;
		}
		for (NameValuePair q : queries) {
			if (q == null) {
				continue;
			}
			String key = q.getName();
			if (!params.containsKey(key)) {
				params.put(key, new ArrayList<String>());
			}
			((List<String>) params.get(key)).add(q.getValue());
		}
		return params;
	}
	
	public static List<NameValuePair> parseQueryAsPairs(String queryString) {
        List<NameValuePair> queries;
        if ((queries = URLEncodedUtils.parse(StringUtils.trimToEmpty(queryString),
                Charset.forName("UTF-8"))) == null) {
            queries = Collections.emptyList();
        }
        return queries;
    }

	public static Object[] fromEnumeration(Enumeration<?> enu) {
		if (enu == null) {
			return new Object[0];
		}
		List<Object> elements = new ArrayList<>();
		while (enu.hasMoreElements()) {
			elements.add(enu.nextElement());
		}

		return elements.toArray();
	}

	protected List<NameValuePair> paramsToNameValuePairs(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return Collections.emptyList();
		}
		List<NameValuePair> pairs = new LinkedList<>();
		for (Map.Entry<String, Object> p : params.entrySet()) {
			String key = p.getKey();
			Object val = p.getValue();
			if (val instanceof Object[]) {
				for (Object v : (Object[]) val) {
					if (v == null) {
						continue;
					}
					if ("Content-Type".equalsIgnoreCase(key)) {
						pairs.add(new BasicNameValuePair(key, v.toString()));
					}else {
						pairs.add(new BasicNameValuePair(key, v.toString()));
					}
				}
			} else if (val instanceof Collection) {
				for (Object v : (Collection<?>) val) {
					if (v == null) {
						continue;
					}
					pairs.add(new BasicNameValuePair(key, v.toString()));
				}
			} else if (val != null) {
				pairs.add(new BasicNameValuePair(key, val.toString()));
			}
		}
		return pairs;
	}

	protected HttpUriRequest build(String url, String method,
            Map<String, Object> params, Map<String, Object> headers,
            HttpEntity bodyEntity, RequestConfig config) throws IOException {
        RequestBuilder builder = RequestBuilder.create(method).setUri(url);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> p : params.entrySet()) {
                String key = p.getKey();
                Object val = p.getValue();
                if (val instanceof Object[]) {
                    for (Object v : (Object[]) val) {
                        if (v == null) {
                            continue;
                        }
                        builder.addParameter(key, v.toString());
                    }
                } else if (val instanceof Collection) {
                    for (Object v : (Collection<?>) val) {
                        if (v == null) {
                            continue;
                        }
                        builder.addParameter(key, v.toString());
                    }
                } else if (val != null) {
                    builder.addParameter(key, val.toString());
                }
            }
        }
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> p : headers.entrySet()) {
                String key = p.getKey();
                Object val = p.getValue();
                if (val instanceof Object[]) {
                    for (Object v : (Object[]) val) {
                        if (v == null) {
                            continue;
                        }
                        builder.addHeader(key, v.toString());
                    }
                } else if (val instanceof Collection) {
                    for (Object v : (Collection<?>) val) {
                        if (v == null) {
                            continue;
                        }
                        builder.addHeader(key, v.toString());
                    }
                } else if (val != null) {
                    builder.addHeader(key, val.toString());
                }
            }
        }
        if (bodyEntity != null) {
			InputStream bodyStream;
			if (!"org.apache.http.entity.mime.MultipartFormEntity".equals(bodyEntity.getClass().getName()) &&
					(bodyStream = bodyEntity.getContent()) != null && bodyStream.markSupported()) {
				bodyStream.reset();
			}
            builder.setEntity(bodyEntity);
        }
        return builder.setConfig(config).build();
    }
	
	public HttpUriRequest build(String url, String method,
			Map<String, Object> params, Map<String, Object> headers,
			byte[] body, RequestConfig config) throws IOException {
	    HttpEntity bodyEntity = null;
	    if (body != null) {
	        bodyEntity = new ByteArrayEntity(body);
	    }
	    return build(url, method, params, headers, bodyEntity, config);
	}
    
    public HttpUriRequest build(String url, String method, Map<String, Object> params) throws IOException {
        return build(url, method, params, null, (byte[]) null, null);
    }
    
    public HttpUriRequest build(String url, String method, byte[] body) throws IOException {
        return build(url, method, null, null, body, null);
    }
    
    public HttpUriRequest build(String url, String method, Map<String, Object> params, byte[] body)
            throws IOException {
        return build(url, method, params, null, body, null);
    }
    
    public HttpUriRequest build(String url, String method, Map<String, Object> params, RequestConfig config)
            throws IOException {
        return build(url, method, params, null, (byte[]) null, config);
    }
    
    public HttpUriRequest build(String url, String method, Map<String, Object> params,
            Map<String, Object> headers) throws IOException {
        return build(url, method, params, headers, (byte[]) null, null);
    }
    
    public CloseableHttpResponse request(String url, String method) throws IOException {
        return request(url, method, null, null, 0);
    }
    
    public CloseableHttpResponse request(String url, String method, int timeout) throws IOException {
        return request(url, method, null, null, timeout);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> params)
            throws IOException {
        return request(url, method, params, null, 0);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> params, int timeout)
            throws IOException {
        return request(url, method, params, null, timeout);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> params,
            Map<String, Object> headers) throws IOException {
        return request(url, method, params, headers, (byte[]) null, 0);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> params,
            Map<String, Object> headers, int timeout) throws IOException {
        return request(url, method, params, headers, (byte[]) null, timeout);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> headers, byte[] body)
            throws IOException {
        return request(url, method, null, headers, body, 0);
    }
    
    public CloseableHttpResponse request(String url, String method, Map<String, Object> params,
            Map<String, Object> headers, byte[] body) throws IOException {
        return request(url, method, params, headers, body, 0);
    }
	
	public CloseableHttpResponse request(String targetUrl,
			HttpServletRequest request, int timeout) throws IOException {
		Map<String, Object> headers = new HashMap<>();
		for (Enumeration<String> h = request.getHeaderNames(); 
		                        h.hasMoreElements();) {
			String n = h.nextElement();
			if ("Content-Length".equalsIgnoreCase(n) || "Transfer-Encoding".equalsIgnoreCase(n)) {
			    continue;
			}
			headers.put(n, fromEnumeration(request.getHeaders(n)));
		}
		return request(targetUrl, request.getMethod(),
				fromQueryString(request.getQueryString()), headers,
				request.getInputStream(),
				timeout);
	}

	public void request(String targetUrl, HttpServletRequest request,
			HttpServletResponse response, int timeout) throws IOException {
		CloseableHttpResponse resp = null;
		try {
			resp = request(targetUrl, request, timeout);
			response.setStatus(resp.getStatusLine().getStatusCode());
			Header[] respHeaders;
			if ((respHeaders = resp.getAllHeaders()) != null) {
				for (Header h : respHeaders) {
				    if ("Transfer-Encoding".equalsIgnoreCase(h.getName())) {
				        continue;
				    }
					response.addHeader(h.getName(), h.getValue());
				}
			}
			IOUtils.copy(resp.getEntity().getContent(),
					response.getOutputStream());
		} finally {
			close(resp);
		}
	}
	
	private CloseableHttpResponse request(String url, String method,
            Map<String, Object> params, Map<String, Object> headers,
            InputStream bodyStream, int timeout) throws IOException {
        HttpEntity bodyEntity = null;
        if (bodyStream != null) {
            if (bodyStream.markSupported()) {
                bodyStream.reset();
            }
            bodyEntity = new InputStreamEntity(bodyStream);
        }
        return request(url, method, params, headers, bodyEntity, timeout);
	}
	
	public CloseableHttpResponse request(String url, String method,
			Map<String, Object> params, Map<String, Object> headers,
			HttpEntity bodyEntity, int timeout) throws IOException {
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT_MS;
		}
		long started = new Date().getTime();
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeout).setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout).build();
		IOException throwable = null;
		CloseableHttpResponse res = null;
		HttpUriRequest req = build(url, method, params, headers, bodyEntity,
				requestConfig);
		byte[] respData = null;
		try {
			res = getHttpClient().execute(req);
			HttpEntity entity = res.getEntity();
			Object contentType = null;
			if (entity != null && (contentType = entity.getContentType()) != null) {
				contentType = ((Header) contentType).getValue();
			}
			respData = LOG_RESPONSED_DATA_SKIPPED;
            if (StringUtils.isNotBlank((String) contentType)
                    && (StringUtils.containsIgnoreCase((String) contentType, "text")
                            || StringUtils.containsIgnoreCase((String) contentType, "json")
                            || StringUtils.containsIgnoreCase((String) contentType, "html")
                            || StringUtils.containsIgnoreCase((String) contentType, "xml"))) {
				respData = getResponseData(res);
				/* 确保可二次读取响应内容 */
				if (entity.isRepeatable() && entity.isStreaming()) {
					entity.getContent().reset();
				} else {
					ByteArrayEntity bEntity;
					res.setEntity(bEntity = new ByteArrayEntity(respData));
					bEntity.setContentEncoding(entity.getContentEncoding());
				}
			}
		} catch (Exception e) {
			throwable = new IOException(e);
		}
		try {
			if (respData == null || respData.length > DEFAULT_MAX_LOG_BODY) {
				respData = LOG_RESPONSED_DATA_SKIPPED;
			}
			byte[] bodyData = null;
            InputStream bodyStream;
			if (bodyEntity != null && !"org.apache.http.entity.mime.MultipartFormEntity".equals(bodyEntity.getClass().getName())
					&&(bodyStream = bodyEntity.getContent()) != null) {
			    bodyData = LOG_RESPONSED_DATA_SKIPPED;
			    if (bodyStream.markSupported()) {
                    int length;
			        bodyStream.reset();
                    if ((length = IOUtils.read(bodyStream,
                            (bodyData = new byte[DEFAULT_MAX_LOG_BODY]))) < DEFAULT_MAX_LOG_BODY) {
                        bodyData = ArrayUtils.subarray(bodyData, 0, length);
                    }
			    }
 			}
			Header[] respHeaders = null;
			StatusLine respStatus = null;
			if (res != null) {
				respStatus = res.getStatusLine();
				respHeaders = res.getAllHeaders();
			}
	        String username = null;
	        if (SessionContext.hasTokenSession()) {
	            String proxyuser = null;
	            username = SessionContext.getTokenUsername();
	            if (StringUtils.isNotBlank(proxyuser = SessionContext.getProxyUsername())) {
	                username = String.format("%s(Proxy by %s)", username, proxyuser);
	            }
	        }
            log.info("HTTP request : username = {}, method = {}, url={}, configs={}, headers={}, params={}"
                    + ", body={}, respstatus = {}, respheaders={}, response={}, time={}",
                username, method, url, requestConfig,
                CommonUtil.replaceSensitive(CommonUtil.toJson(headers)),
                CommonUtil.replaceSensitive(CommonUtil.toJson(params)),
                CommonUtil.replaceSensitive(CommonUtil.bytesToDisplay(bodyData)),
                respStatus, respHeaders,
                CommonUtil.replaceSensitive(CommonUtil.bytesToDisplay(respData)),
                System.currentTimeMillis() - started
            );
		} catch (Exception e) {
			log.warn(e.toString(), e);
		}
		if (throwable != null) {
		    close(res);
			throw throwable;
		}
		return res;
	}

	public CloseableHttpResponse request(String url, String method,
			Map<String, Object> params, Map<String, Object> headers,
			byte[] body, int timeout) throws IOException {
	    ByteArrayEntity bodyEntity = null;
		if (body != null) {
		    bodyEntity = new ByteArrayEntity(body);
		}
		return request(url, method, params, headers, bodyEntity, timeout);
	}

	public CloseableHttpResponse request(HttpUriRequest req)
			throws ClientProtocolException, IOException {
		return HttpClients.createDefault().execute(req);
	}

	public static byte[] getResponseData(@NonNull CloseableHttpResponse resp)
			throws IOException {
		InputStream is = resp.getEntity().getContent();
		return IOUtils.toByteArray(is);
	}

	public static JsonElement getResponseJson(CloseableHttpResponse resp)
			throws IOException {
		if (resp.getEntity() == null) {
			return null;
		}
		return getResponseJson(resp, null);
	}

	public static JsonElement getResponseJson(CloseableHttpResponse resp,
			String charset) throws IOException {
		return CommonUtil.fromJson(getResponseText(resp, charset),
				JsonElement.class);
	}

	public static <T> T getResponseJson(CloseableHttpResponse resp,
			String charset, Class<T> clazz) throws IOException {
		return CommonUtil.fromJson(getResponseText(resp, charset), clazz);
	}

	public static String getResponseText(@NonNull CloseableHttpResponse resp)
			throws IOException {
		return getResponseText(resp, null);
	}
	
	public static String getResponseText(@NonNull CloseableHttpResponse resp,
			String charset) throws IOException {
		if (StringUtils.isBlank(charset)) {
			charset = "UTF-8";
		}
		return new String(getResponseData(resp), charset);
	}
	
	public static int getStatusCode(@NonNull CloseableHttpResponse resp) {
	    return resp.getStatusLine().getStatusCode();
	}
	
	public CloseableHttpResponse get(String url) throws IOException {
		return request(url, "GET", null);
	}
	
	public CloseableHttpResponse get(String url, int timeout)
			throws IOException {
		return request(url, "GET", null, timeout);
	}
	
	public CloseableHttpResponse get(String url,
			Map<String, Object> params) throws IOException {
		return request(url, "GET", params, 0);
	}
	
	public CloseableHttpResponse get(String url,
			Map<String, Object> params, int timeout) throws IOException {
		return request(url, "GET", params, timeout);
	}
	
	public CloseableHttpResponse postJson(String url, Object data)
			throws IOException {
		byte[] body = null;
		if (data != null) {
			body = StringUtils.trimToEmpty(CommonUtil.toJson(data)).getBytes(
					"UTF-8");
		}
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON.toString());
		return request(url, "POST", null, headers, body, 0);
	}
	
	public CloseableHttpResponse postJson(String url, Object data,
			int timeout) throws IOException {
		return postJson(url, null, data, timeout);
	}

	public CloseableHttpResponse postJson(String url,
			Map<String, Object> headers, Object data) throws IOException {
		return postJson(url, headers, data, 0);
	}

	public CloseableHttpResponse postJson(String url,
			Map<String, Object> headers, Object data, int timeout)
			throws IOException {
		byte[] body = null;
		if (data != null) {
			body = StringUtils.trimToEmpty(CommonUtil.toJson(data)).getBytes(
					"UTF-8");
		}
		if (headers == null) {
			headers = new HashMap<String, Object>();
		}
		headers.put("Content-Type", ContentType.APPLICATION_JSON.toString());
		return request(url, "POST", null, headers, body, timeout);
	}

	public CloseableHttpResponse post(String url) throws IOException {
		return post(url, null, null, 0);
	}

	public CloseableHttpResponse post(String url, int timeout)
			throws IOException {
		return post(url, null, null, timeout);
	}

	public CloseableHttpResponse post(String url,
			Map<String, Object> params) throws IOException {
		return post(url, params, null, 0);
	}

	public CloseableHttpResponse post(String url,
			Map<String, Object> params, int timeout) throws IOException {
		return post(url, params, null, timeout);
	}

	public CloseableHttpResponse post(String url,
			Map<String, Object> params, Map<String, Object> headers, int timeout)
			throws IOException {
		if (headers == null) {
			headers = new HashMap<String, Object>();
		}
		headers.put("Content-Type",
				ContentType.APPLICATION_FORM_URLENCODED.toString());
		byte[] body = toQueryString(params).getBytes("UTF-8");
		return request(url, "POST", null, headers, body, timeout);
	}

	public static void close(CloseableHttpResponse resp) {
		if (resp != null) {
			try {
				resp.close();
			} catch (Exception e) {

			}
		}
	}
	
	protected final HostnameVerifier AllowAllHostnameVerifier = new HostnameVerifier() {
        @Override
        public final String toString() {
            return "ALLOW_ALL";
        }
        
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

	protected CloseableHttpClient getHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 默认信任所有证书
                @Override
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(sslContext, AllowAllHostnameVerifier);
            return HttpClients.custom().setSSLSocketFactory(sslcsf).build();
        } catch (Exception e) {
            
        }
        return HttpClients.createDefault();
    }
}
