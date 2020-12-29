package com.weimob.webfwk.util.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractWeimobNotifyService extends AbstractExternalHttpService {
    
    protected abstract String getAppKey();
    
    protected abstract String getTemplateId();
    
    public final static int OPTION_NOEXCEPTOIN_ONERROR = 1;
    
    public boolean send(String content, String[] receivers, int options) throws Exception {
        try {
            JsonElement result = postJson("/info/sent",
                    (Object)(new ObjectMap().put("appKey", getAppKey()).put("templateId", getTemplateId())
                            .put("content", CommonUtil.toJson(new ObjectMap().put("content", content).asMap()))
                            .put("receivers", receivers).asMap()), null);
            if (result == null || !result.isJsonObject()
                    || !"000000".equals(CommonUtil.getJstring((JsonObject) result, ""))) {
                throw new RuntimeException("通知发送失败");
            }
            return true;
        } catch (Exception e) {
            log.error(e.toString(), e);
            if ((OPTION_NOEXCEPTOIN_ONERROR & options) != 0) {
                return false;
            }
            throw e;
        }
    }
}
