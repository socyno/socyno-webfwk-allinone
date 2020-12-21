package org.socyno.webfwk.state.controller;


import org.socyno.webfwk.state.service.SimpleTodoService;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.remote.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
