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
@RequestMapping(value = "/config")
public class SystemConfigController {
    
    @Authority(value = AuthorityScopeType.Guest)
    @RequestMapping(value = "/externals", method = RequestMethod.GET)
    public void listExternals(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/config/externals", req, res);
    }
}
