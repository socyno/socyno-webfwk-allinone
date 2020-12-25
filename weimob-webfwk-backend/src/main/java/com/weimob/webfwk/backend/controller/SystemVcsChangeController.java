package com.weimob.webfwk.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weimob.webfwk.module.vcs.change.VcsChangeController;

@RestController 
@RequestMapping(value="/api/vcschange")
public class SystemVcsChangeController extends VcsChangeController {

}
