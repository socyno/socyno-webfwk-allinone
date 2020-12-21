package org.socyno.webfwk.backend.controller;

import org.socyno.webfwk.state.controller.MyTodoController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping(value="/api/mytodo")
public class SystemMyTodoController extends MyTodoController {

}
