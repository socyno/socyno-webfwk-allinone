package com.weimob.webfwk.state.module.user;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.state.module.user.WindowsAdService.SystemWindowsAdUser;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WeimobSsoService {
    
    public static SystemWindowsAdUser validateTicket(String ticket, String ssoService)
            throws UnsupportedEncodingException, IOException {
        String validateUrl = ContextUtil.getConfigTrimed("system.user.login.weimob.sso.validate.url");
        String validateService = CommonUtil.ifBlank(ssoService,
                ContextUtil.getConfigTrimed("system.user.login.weimob.sso.validate.service"));
        CloseableHttpResponse resp = null;
        try {
            resp = HttpUtil.get(
                    String.format("%s?service=%s&ticket=%s", validateUrl, HttpUtil.urlEncode(validateService), ticket));
            JsonElement json = HttpUtil.getResponseJson(resp);
            String username = "";
            String display = "";
            String mailAddress = "";
            if (json != null && json.isJsonObject()
                    && "success".equalsIgnoreCase(CommonUtil.getJstring((JsonObject) json, "retval"))
                    && StringUtils.isNotBlank(mailAddress = CommonUtil.getJstring((JsonObject) json, "email"))
                    && StringUtils.isNotBlank(username = CommonUtil.getJstring((JsonObject) json, "username"))
                    && StringUtils.isNotBlank(display = CommonUtil.getJstring((JsonObject) json, "given_name"))) {
                SystemWindowsAdUser winadUser = new SystemWindowsAdUser();
                winadUser.setLogin(username);
                winadUser.setName(display);
                winadUser.setMail(mailAddress);
                return winadUser;
            }
        } finally {
            HttpUtil.close(resp);
        }
        return null;
    }
}
