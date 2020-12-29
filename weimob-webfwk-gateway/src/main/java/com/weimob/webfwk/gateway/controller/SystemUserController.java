package com.weimob.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.gateway.util.HttpRedirectUtil;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.*;
import com.weimob.webfwk.state.module.user.SystemUserFormLogin;

@RestController
@RequestMapping(value = "/user")
public class SystemUserController {
    
    @Attributes(title = "用户登录")
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(@RequestBody SystemUserFormLogin form, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/user/login", req, res);
    }
    
    @Attributes(title = "用户登出")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/user/logout", req, res);
    }
    
    @Attributes(title = "用户菜单")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/menus", method = RequestMethod.GET)
    public void menus(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/user/menus", req, res);
    }
    
    @Attributes(title = "我的待处理待办事项清单")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/list", method = RequestMethod.GET)
    public void todoListMyOpened(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/list", req, res);
    }
    
    @Attributes(title = "我的待处理待办事项数量")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/total", method = RequestMethod.GET)
    public void todoTotalMyOpened(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/total", req, res);
    }
    
    @Attributes(title = "我发起的待办事项清单")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/applied", method = RequestMethod.GET)
    public void todoListMyApplied(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/applied", req, res);
    }
    
    @Attributes(title = "我处理的待办事项清单" )
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/closed", method = RequestMethod.GET)
    public void todoListMyClosed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/closed", req, res);
    }
    
    @Attributes(title = "关闭待办事项记录" )
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/close", method = RequestMethod.POST)
    public void todoClose(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/close", req, res);
    }
    
    @Attributes(title = "创建待办事项记录" )
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/create", method = RequestMethod.POST)
    public void todoCreate(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/create", req, res);
    }
    
    @Attributes(title = "应用收藏")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/app/bookmark/add/{applicationId}", method = RequestMethod.POST)
    public void appBookmarkAdd(@PathVariable long applicationId, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/sys/applications/%s/bookmark/add", applicationId), req,
                res);
    }
    
    @Attributes(title = "取消应用收藏")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/app/bookmark/delete/{applicationId}", method = RequestMethod.POST)
    public void appBookmarkDelete(@PathVariable long applicationId, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/sys/applications/%s/bookmark/delete", applicationId),
                req, res);
    }
}
