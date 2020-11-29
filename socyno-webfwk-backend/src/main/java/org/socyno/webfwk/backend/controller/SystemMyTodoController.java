package org.socyno.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.socyno.webfwk.module.basic.MyTodoController;

@RestController 
@RequestMapping(value="/api/mytodo")
public class SystemMyTodoController extends MyTodoController {

}
