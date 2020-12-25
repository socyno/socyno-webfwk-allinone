package com.weimob.webfwk.state.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.weimob.webfwk.state.service.SimpleTodoService;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.remote.R;

public class MyTodoController {
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R getTodoList() throws Exception {
        return R.ok().setData(SimpleTodoService.queryOpenedByAssignee(SessionContext.getUserId()));
    }
    
    @RequestMapping(value = "/total", method = RequestMethod.GET)
    public R getUserTodoTotal() throws Exception {
        return R.ok().setData(SimpleTodoService.queryOpenedCountByAssignee(SessionContext.getUserId()));
    }

    @RequestMapping(value = "/created", method = RequestMethod.GET)
    public R getCreatedTodoList(Integer page , Integer limit) throws Exception {
        return R.ok().setData(SimpleTodoService.queryTodoByCreator(SessionContext.getUserId() , page , limit));
    }

    @RequestMapping(value = "/closed", method = RequestMethod.GET)
    public R getClosedTodoList(Integer page , Integer limit) throws Exception {
        return R.ok().setData(SimpleTodoService.queryTodoByCloser(SessionContext.getUserId(), page , limit));
    }

}
