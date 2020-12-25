package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.state.controller.UserController;

@RestController 
@RequestMapping(value="/api/user")
public class SystemUserController extends UserController {

}
