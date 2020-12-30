package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.state.controller.ConfigController;

@RestController 
@RequestMapping(value="/api/config")
public class SystemConfigController extends ConfigController {

}
