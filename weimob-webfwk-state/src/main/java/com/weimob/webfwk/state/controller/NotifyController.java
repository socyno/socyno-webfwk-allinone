package com.weimob.webfwk.state.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.weimob.webfwk.state.module.notify.SystemNotifyService;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.tool.CommonUtil;

public class NotifyController {
    
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public R send(@RequestParam("templateId") String templateId, @RequestParam("synchronized") String synced,
            @RequestBody Map<String, Object> context) throws Exception {
        if (CommonUtil.parseBoolean(synced)) {
            SystemNotifyService.getInstance().sendSync(templateId, context, 0);
        } else {
            SystemNotifyService.getInstance().sendAsync(templateId, context, 0);
        }
        return R.ok();
    }
    
}