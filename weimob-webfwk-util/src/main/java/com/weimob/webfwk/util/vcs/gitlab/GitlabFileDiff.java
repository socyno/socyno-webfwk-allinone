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
public class GitlabFileDiff {
    private String  newPath;
    private String  oldPath;
    private String  diff;
    private boolean newFile = false;
    private boolean renamedFile = false;
    private boolean deletedFile = false;
    
    private final static String[] fieldsRequired = new String[] {
        "new_path", "old_path", "diff"
    };
    
    private final static String[] fieldsOptional = new String[] {
        "new_file", "renamed_file", "deleted_file"
    };
    
    private final static String[] fieldsToBoolean = new String[] {
        
    };
    
    public static GitlabFileDiff fromJson(@NonNull JsonObject data)
                        throws IllegalJsonDataException, Exception {
        Map<String, Object> mapd = new HashMap<String, Object>();
        for (String a : fieldsRequired) {
            String mapv;
            if ((mapv= CommonUtil.getJstring(data, a)) == null) {
                throw new IllegalJsonDataException(String.format(
                        "The attribute named '%s' is not found.", a));
            }
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
        }
        for (String a : fieldsOptional) {
            String mapv = CommonUtil.getJstring(data, a);
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
        }
        for (String a : fieldsToBoolean) {
            String mapv = (String)mapd.get(a);
            if (StringUtils.isBlank(mapv)) {
                continue;
            }
            mapd.put(a, CommonUtil.parseBoolean(mapv));
        }
        return CommonUtil.toBean(mapd, GitlabFileDiff.class);
    }
}
