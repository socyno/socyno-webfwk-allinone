package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.state.controller.InternalController;

@RestController 
@RequestMapping(value="/api/internal")
public class SystemInternalController extends InternalController {

}
