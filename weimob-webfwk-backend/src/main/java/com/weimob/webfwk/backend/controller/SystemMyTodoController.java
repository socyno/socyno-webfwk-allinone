package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.state.controller.MyTodoController;

@RestController 
@RequestMapping(value="/api/mytodo")
public class SystemMyTodoController extends MyTodoController {

}
