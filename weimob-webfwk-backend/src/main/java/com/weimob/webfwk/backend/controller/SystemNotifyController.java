package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.state.controller.NotifyController;

@RestController 
@RequestMapping(value="/api/notify")
public class SystemNotifyController extends NotifyController {

}
