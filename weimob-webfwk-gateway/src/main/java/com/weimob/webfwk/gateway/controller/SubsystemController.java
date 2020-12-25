package com.weimob.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.gateway.util.HttpRedirectUtil;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;

@RestController
@RequestMapping(value = "/sys")
public class SubsystemController {
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/options/user", method = RequestMethod.GET)
    public void queryUsersAsOption(String namelike, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/options/user", req, res);
    }
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/options/subsystem", method = RequestMethod.GET)
    public void querySubsystemsAsOption(String namelike, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/options/subsystem", req, res);
    }
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/options/application", method = RequestMethod.GET)
    public void queryApplicationsAsOption(String namelike, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/options/application", req, res);
    }
    
    @Attributes(title = "获取应用部署环境可选项")
    @RequestMapping(value = "/options/appenv/{applicationId}", method = RequestMethod.GET)
    @Authority(value = AuthorityScopeType.System)
    public void queryApplicationEnv(@PathVariable long applicationId , HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/sys/options/appenv/%s", applicationId), request, response);
    }
    
    @Attributes(title = "可见应用清单查询")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/applications/visible", method = RequestMethod.GET)
    public void queryVisibleApps(Boolean bookmarked, String namelike, String type, Long subsystemId, String codeLevel,
            Integer limit, Integer page, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/applications/visible", req, res);
    }
    
    @Attributes(title = "所有应用清单查询")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/applications/list", method = RequestMethod.GET)
    public void qeueryAllApps(String namelike, String type, Long subsystemId, String codeLevel,
            Integer limit, Integer page, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/applications/list", req, res);
    }
    
    @Attributes(title = "获取应用基本信息")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/applications/{applicationId}/simple", method = RequestMethod.GET)
    public void getAppSimple(@PathVariable Long applicationId, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/sys/applications/%s/simple", applicationId), req,
                res);
    }
    
    @Attributes(title = "获取应用部署机组")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/applications/{applicationId}/deploy/namespaces", method = RequestMethod.GET)
    public void getAppDeployNamespaces(@PathVariable Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(
                String.format("/api/sys/applications/%s/deploy/namespaces", applicationId), req, res);
    }
    
    @Attributes(title = "产品组/线清单")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/productlines/list", method = RequestMethod.GET)
    public void queryProductlines(String namelike, Long applicationId, Long subsystemId, Long ownerId, Integer limit, Integer page, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/sys/productlines/list", req, res);
    }
    
    @Attributes(title = "获取业务基本信息")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/subsystems/{subsystemId}/simple", method = RequestMethod.GET)
    public void getSubsystemSimple(@PathVariable Long subsystemId, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/sys//subsystems/%s/simple", subsystemId), req, res);
    }
}
