package org.socyno.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.gateway.util.HttpRedirectUtil;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.*;
import org.socyno.webfwk.state.module.user.SystemUserFormLogin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.github.reinert.jjschema.Attributes;

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
    
    @Attributes(title = "List:用户待办事项列表")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/list", method = RequestMethod.GET)
    public void myTodoList(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/list", req, res);
    }

    @Attributes(title = "用户待办事项数量")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/total", method = RequestMethod.GET)
    public void myTodoTotal(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/total", req, res);
    }

    @Attributes(title = "我创建的待办事项列表")
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/created", method = RequestMethod.GET)
    public void myCreatedTodoList(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/created", req, res);
    }

    @Attributes(title = "我申请的待办事项列表" )
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/mytodo/closed", method = RequestMethod.GET)
    public void myClosedTodoList(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/mytodo/closed", req, res);
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
