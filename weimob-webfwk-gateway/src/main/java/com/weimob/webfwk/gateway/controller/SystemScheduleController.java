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
@RequestMapping(value = "/schedules")
public class SystemScheduleController {
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public void listSchedules(String namelike, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToSchedule("/api/schedules/list", req, res);
    }
}
