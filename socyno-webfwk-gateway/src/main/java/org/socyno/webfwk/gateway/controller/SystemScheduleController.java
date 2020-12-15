package org.socyno.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.gateway.util.HttpRedirectUtil;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/schedules")
public class SystemScheduleController {
    
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public void listSchedules(String namelike, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToSchedule("/api/schedules/list", req, res);
    }
}
