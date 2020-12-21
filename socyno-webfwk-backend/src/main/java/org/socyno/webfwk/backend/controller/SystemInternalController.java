package org.socyno.webfwk.backend.controller;

import org.socyno.webfwk.state.controller.InternalController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping(value="/api/internal")
public class SystemInternalController extends InternalController {

}
