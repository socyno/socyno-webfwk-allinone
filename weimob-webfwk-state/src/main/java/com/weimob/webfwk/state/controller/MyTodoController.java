package com.weimob.webfwk.state.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.weimob.webfwk.state.service.SimpleTodoService;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.remote.R;

public class MyTodoController {
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R listOpened() throws Exception {
        return R.ok().setData(SimpleTodoService.queryOpenedByAssignee(SessionContext.getUserId()));
    }
    
    @RequestMapping(value = "/total", method = RequestMethod.GET)
    public R getOpenedTotal() throws Exception {
        return R.ok().setData(SimpleTodoService.queryOpenedCountByAssignee(SessionContext.getUserId()));
    }
    
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public R create(String targetKey, String targetId, String applierCode, String title, String category,
            String[] assigneeCodes) throws Exception {
        SimpleTodoService.createTodoUsePageTmpl(targetKey, targetId, applierCode, title, category, assigneeCodes);
        return R.ok();
    }
    
    @RequestMapping(value = "/applied", method = RequestMethod.GET)
    public R listApplied(Integer page, Integer limit) throws Exception {
        return R.ok().setData(SimpleTodoService.queryTodoByApplier(SessionContext.getUserId(), page, limit));
    }
    
    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public R close(String targetKey, String targetId, String result) throws Exception {
        SimpleTodoService.closeTodo(targetKey, targetId, result);
        return R.ok();
    }
    
    @RequestMapping(value = "/closed", method = RequestMethod.GET)
    public R listClosed(Integer page, Integer limit) throws Exception {
        return R.ok().setData(SimpleTodoService.queryTodoByCloser(SessionContext.getUserId(), page, limit));
    }
    
}
