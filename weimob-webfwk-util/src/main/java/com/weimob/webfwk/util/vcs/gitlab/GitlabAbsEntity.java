package com.weimob.webfwk.util.vcs.gitlab;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;

@Getter
public abstract class GitlabAbsEntity {

    protected abstract String[] getRequiredFields();
    
    protected abstract String[] getOptionalFields();
    
    protected abstract String[] getLongFields();
    
    protected <T extends GitlabAbsEntity> T fromJson(JsonElement data, Class<T> clazz)
                        throws IllegalJsonDataException, Exception {
        if (data == null || !data.isJsonObject()) {
            throw new IllegalJsonDataException(
                    "The provided json is not valid object.");
        }
        Map<String, Object> mapd = new HashMap<String, Object>();
        String[] fields ;
        if ((fields = getRequiredFields()) != null) {
            for (String a : fields) {
                String mapv = CommonUtil.getJstring((JsonObject)data, a);
                mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
                if (StringUtils.isBlank(mapv)) {
                    throw new IllegalJsonDataException(String.format(
                            "The attribute named '%s' is not found.", a));
                }
            }
        }
        if ((fields = getOptionalFields()) != null) {
            for (String a : fields) {
                String mapv = CommonUtil.getJstring((JsonObject)data, a);
                if (StringUtils.isBlank(mapv)) {
                    continue;
                }
                mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
            }
        }
        if ((fields = getLongFields()) != null) {
            for (String a : fields) {
                String mapv = (String)mapd.get(a);
                if (StringUtils.isBlank(mapv)) {
                    continue;
                }
                mapd.put(a, CommonUtil.parseLong(mapv));
            }
        }
        return CommonUtil.toBean(mapd, clazz);
    }
}
