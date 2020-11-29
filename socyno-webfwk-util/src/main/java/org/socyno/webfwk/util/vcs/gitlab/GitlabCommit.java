package org.socyno.webfwk.util.vcs.gitlab;

import java.util.HashMap;
import java.util.Map;

import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
public class GitlabCommit {
    private String  id;
    private String  title;
    private String  message;
    private String  shortId;
    private String[]  parentIds;
    private String  authorName;
    private String  authorEmail;
    private String  authoredDate;
    
    private String  committerName;
    private String  committerEmail;
    private String  committedDate;
    
    public static GitlabCommit fromJson(@NonNull JsonObject data)
                        throws IllegalJsonDataException, Exception {
        Map<String, Object> mapd = new HashMap<String, Object>();
        for (String a : new String[]{"id", "short_id", "title", "message",
                        "author_name", "author_email", "authored_date",
                        "committer_name", "committer_email", "committed_date"}) {
            String mapv = CommonUtil.getJstring(data, a);
            mapd.put(CommonUtil.applyFieldNamingPolicy(a), mapv);
            if (StringUtils.isBlank(mapv)) {
                throw new IllegalJsonDataException(String.format(
                        "The attribute named '%s' is not found.", a));
            }
        }
        JsonElement parentIds;
        String[] parents = new String[0];
        if ((parentIds = data.get("parentIds")) != null) {
            if (!parentIds.isJsonArray()) {
                throw new IllegalJsonDataException();
            }
            parents = new String[((JsonArray)parentIds).size()];
            for (int i =0 ; i < parents.length; i++) {
                parents[i] = ((JsonArray)parentIds).get(i).getAsString();
            }
        }
        mapd.put("parentIds", parents);
        return CommonUtil.toBean(mapd, GitlabCommit.class);
    }
}
