package org.socyno.webfwk.gateway.util;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.state.service.StateFormService;
import org.socyno.webfwk.state.service.StateFormService.StateFormRegister;
import org.socyno.webfwk.state.abs.AbstractStateFormService;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.exception.PageNotFoundException;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class HttpRedirectUtil {
    
    private static String getBackendUrl() {
//        return "http://localhost:8080/webfwk-backend";
        return CommonUtil.ifBlank(ContextUtil.getConfig("system.gateway.backend.service.url"),
                "http://localhost:8080/webfwk-backend/");
    }
    
    private static String getExecutorUrl() {
//        return "http://localhost:8080/webfwk-executor";
        return CommonUtil.ifBlank(ContextUtil.getConfig("system.gateway.executor.service.url"),
                "http://localhost:8080/webfwk-executor/");
    }
    
    private static String getScheduleUrl() {
//        return "http://localhost:8080/webfwk-schedule";
        return CommonUtil.ifBlank(ContextUtil.getConfig("system.gateway.schedule.service.url"),
                "http://localhost:8080/webfwk-schedule/");
    }
    
    public static int getRequestTimeouMs() {
        return CommonUtil.parseInteger(ContextUtil.getConfigTrimed("system.gateway.backend.timeout"), 60000);
    }
    
    public static void redirectToBackend(String targetUrl, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        redirectToBackend(ServiceBackend.Backend, targetUrl, req, res);
    }
    
    public static void redirectToSchedule(String targetUrl, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        redirectToBackend(ServiceBackend.Schedule, targetUrl, req, res);
    }
    
    public static enum ServiceBackend {
        Backend() {
            @Override
            public String getRequestRootUrl() {
                return HttpRedirectUtil.getBackendUrl();
            }
        },
        Executor() {
            @Override
            public String getRequestRootUrl() {
                return HttpRedirectUtil.getExecutorUrl();
            }
        },
        Schedule() {
            @Override
            public String getRequestRootUrl() {
                return HttpRedirectUtil.getScheduleUrl();
            }
        };

        public static ServiceBackend getWebSocket(String pathStarts) throws Exception {
            if (StringUtils.isBlank(pathStarts)) {
                return null;
            }
            for (ServiceBackend backend : ServiceBackend.values()) {
                if (new URI(backend.getRequestRootUrl()).getPath().startsWith(pathStarts)) {
                    return backend;
                }
            }
            return null;
        }
        
        public abstract String getRequestRootUrl();
        
        public int getRequestTimeoutMS() {
            return HttpRedirectUtil.getRequestTimeouMs();
        }
        
        public static ServiceBackend getForm(String formName) throws Exception {
            if (StringUtils.isBlank(formName)) {
                return null;
            }
            /* 检查用户是否有流程访问权限 */
            if (!PermissionService.hasAnyPermission(AbstractStateFormService.getFormAccessEventKey(formName))) {
                return null;
            }
            StateFormRegister register;
            if ((register = StateFormService.getFormRegister(formName)) == null || !register.isEnabled()) {
                return null;
            }
            for (ServiceBackend backend : ServiceBackend.values()) {
                if (backend.name().equalsIgnoreCase(register.getFormBackend().replaceAll("[-_]+", ""))) {
                    return backend;
                }
            }
            return null;
        }
    }
    
    public static void redirectToBackend(ServiceBackend backend, String targetUrl, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        if (backend == null || StringUtils.isBlank(targetUrl)) {
            throw new PageNotFoundException();
        }
        HttpUtil.request(HttpUtil.concatUrlPath(backend.getRequestRootUrl(), targetUrl), req, res,
                backend.getRequestTimeoutMS());
    }
}
