package org.socyno.webfwk.backend.controller;

import org.socyno.webfwk.state.controller.UserController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping(value="/api/user")
public class SystemUserController extends UserController {

}
