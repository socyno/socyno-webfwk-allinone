package org.socyno.webfwk.util.vcs.gitlab;


import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.JsonElement;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabGroup {
    private Long id;
    private String name;
    private String path;
    private String webUrl;
    private String description;
    private String visibility;
    private Long parentId;
    private String fullName;
    
    public static GitlabGroup fromJson(@NonNull JsonElement data) throws IllegalJsonDataException, Exception {
        GitlabGroup group;
        if ((group = CommonUtil.fromJsonByFieldNamingPolicy(data, GitlabGroup.class)) == null || group.getId() == null
                || StringUtils.isBlank(group.getName()) || StringUtils.isBlank(group.getPath())
                || StringUtils.isBlank(group.getVisibility())) {
            throw new IllegalJsonDataException();
        }
        return group;
    }
}
