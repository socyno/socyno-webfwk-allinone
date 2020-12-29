package com.weimob.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.gateway.util.HttpRedirectUtil;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;

@RestController
@RequestMapping(value = "/notify")
public class SystemNotifyController {
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public void send(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToSchedule("/api/notify/send", req, res);
    }
}
