package com.weimob.webfwk.state.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.weimob.webfwk.state.module.config.SystemConfigFormSimple;
import com.weimob.webfwk.state.module.config.SystemConfigService;
import com.weimob.webfwk.util.remote.R;

public class ConfigController {
    
    @RequestMapping(value = "/externals", method = RequestMethod.GET)
    public R listExternals() throws Exception {
        Map<String, String> configs = new HashMap<>();
        for (SystemConfigFormSimple c : SystemConfigService.getInstance().getAllExternals()) {
            configs.put(c.getName(), c.getValue());
        }
        return R.ok().setData(configs);
    }
    
}
