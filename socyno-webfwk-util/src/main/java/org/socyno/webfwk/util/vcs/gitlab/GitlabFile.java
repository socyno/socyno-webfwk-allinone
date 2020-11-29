package org.socyno.webfwk.util.vcs.gitlab;

import java.util.HashMap;
import java.util.Map;

import org.socyno.webfwk.util.tool.Base64Util;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class GitlabFile {
    private Long    size;
    private String  fileName;
    private String  filePath;
    private String  blobId;
    private String  lastCommitId;
    private String  commitId;
    private byte[]  content;
    
    public static GitlabFile fromJson(JsonObject data)
                        throws IllegalJsonDataException, Exception {
        if (data == null || !data.has("size")
                || !data.has("file_name") || !data.has("file_path")
                || !data.has("blob_id") || !data.has("commit_id")
                || !data.has("last_commit_id") || !data.has("content")) {
            throw new IllegalJsonDataException();
        }
        Long size;
        if ((size = CommonUtil.getJsLong(data, "size")) == null
                    || size < 0) {
            throw new IllegalJsonDataException(
                    "The attribute named 'size' is not found or invalid.");
        }
        Map<String, Object> mapd = new HashMap<String, Object>();
        for (String a : new String[]{"file_name", "file_path", "blob_id", 
                                            "commit_id", "last_commit_id"}) {
            String mapv = CommonUtil.getJstring(data, a);
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
            if (StringUtils.isBlank(mapv)) {
                throw new IllegalJsonDataException(String.format(
                        "The attribute named '%s' is not found.", a));
            }
        }
        mapd.put("size", size);
        byte[] content = new byte[0];
        if (size > 0) {
            String mapv;
            if ((mapv = CommonUtil.getJstring(data, "content")) != null) {
                content = Base64Util.decode(mapv);
            }
            if (content.length != size) {
                throw new IllegalJsonDataException("The size of file content check failed.");
            }
        }
        mapd.put("content", content);
        return CommonUtil.toBean(mapd, GitlabFile.class);
    }
}
