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
public class GitlabCommitReference {
    private String  type;
    private String  name;
    
    private final static String[] fieldsRequired = new String[] {
        "name", "type"
    };
    
    public static GitlabCommitReference fromJson(@NonNull JsonObject data)
                        throws IllegalJsonDataException, Exception {
        Map<String, Object> mapd = new HashMap<String, Object>();
        for (String a : fieldsRequired) {
            String mapv;
            if (StringUtils.isBlank(mapv = CommonUtil.getJstring(data, a))) {
                throw new IllegalJsonDataException(String.format(
                        "The attribute named '%s' is not found.", a));
            }
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
        }
        return CommonUtil.toBean(mapd, GitlabCommitReference.class);
    }
    
    public boolean isBranch() {
        return "branch".equalsIgnoreCase(getType());
    }
    
    public boolean isTag() {
        return "tag".equalsIgnoreCase(getType());
    }
}
