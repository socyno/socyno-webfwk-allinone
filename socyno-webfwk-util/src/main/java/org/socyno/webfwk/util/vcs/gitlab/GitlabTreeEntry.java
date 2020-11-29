package org.socyno.webfwk.util.vcs.gitlab;

import java.util.HashMap;
import java.util.Map;

import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class GitlabTreeEntry {
    private String  id;
    private String  name;
    private String  type;
    private String  path;
    private String  mode;
    
    private final static String[] fieldsRequired = new String[] {
        "id", "name", "type", "path", "mode"
    };
    
    private final static String[] fieldsOptional = new String[] {
        
    };
    
    private final static String[] fieldsToLong = new String[] {
        
    };
    
    public static GitlabTreeEntry fromJson(@NonNull JsonObject data)
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
        return CommonUtil.toBean(mapd, GitlabTreeEntry.class);
    }
}
