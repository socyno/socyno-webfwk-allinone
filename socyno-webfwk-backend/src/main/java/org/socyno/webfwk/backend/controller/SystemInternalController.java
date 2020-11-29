package org.socyno.webfwk.backend.controller;

import org.socyno.webfwk.module.basic.InternalController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping(value="/api/internal")
public class SystemInternalController extends InternalController {

}
