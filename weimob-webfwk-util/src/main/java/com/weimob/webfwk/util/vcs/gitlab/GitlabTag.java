package com.weimob.webfwk.util.vcs.gitlab;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class GitlabTag {
    private String  name;
    private String  target;
    private String  message;
    
    private final static String[] fieldsRequired = new String[] {
        "name", "target"
    };
    
    private final static String[] fieldsOptional = new String[] {
        "message"
    };
    
    private final static String[] fieldsToLong = new String[] {
        
    };
    
    public static GitlabTag fromJson(@NonNull JsonObject data)
                        throws IllegalJsonDataException, Exception {
        Map<String, Object> mapd = new HashMap<String, Object>();
        for (String a : fieldsRequired) {
            String mapv = CommonUtil.getJstring(data, a);
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
            if (StringUtils.isBlank(mapv)) {
                throw new IllegalJsonDataException(String.format(
                        "The attribute named '%s' is not found.", a));
            }
        }
        for (String a : fieldsOptional) {
            String mapv = CommonUtil.getJstring(data, a);
            if (StringUtils.isBlank(mapv)) {
                continue;
            }
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
        }
        for (String a : fieldsToLong) {
            String mapv = (String)mapd.get(a);
            if (StringUtils.isBlank(mapv)) {
                continue;
            }
            mapd.put(a, CommonUtil.parseLong(mapv));
        }
        return CommonUtil.toBean(mapd, GitlabTag.class);
    }
}
