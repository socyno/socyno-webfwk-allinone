package org.socyno.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.socyno.webfwk.module.basic.UserController;

@RestController 
@RequestMapping(value="/api/user")
public class SystemUserController extends UserController {

}
