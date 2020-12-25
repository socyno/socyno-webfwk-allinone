package com.weimob.webfwk.backend.interceptor;

import org.springframework.web.bind.annotation.ControllerAdvice;

import com.weimob.webfwk.util.exception.MvcResponseExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends MvcResponseExceptionHandler {
    
}