package com.weimob.webfwk.util.vcs.gitlab;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.google.gson.JsonObject;
import com.weimob.webfwk.util.tool.CommonUtil;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabProject {
    private long id;
    private String name;
    private String path;
    private String createdAt;
    private Long creatorId;
    private String description;
    private String pathWithNamespace;
    private GitlabUser owner;
    private List<GitlabSharedWithGroup> sharedWithGroups;
    private String sshUrlToRepo;
    private String httpUrlToRepo;
    private String webUrl;
    
    public static GitlabProject fromJson(JsonObject data) throws IllegalJsonDataException {
        if (data == null || !data.has("id") || !data.has("name") || !data.has("web_url") || !data.has("owner")
                || !data.has("path_with_namespace") || !data.has("http_url_to_repo") || !data.has("ssh_url_to_repo")) {
            throw new IllegalJsonDataException();
        }
        return CommonUtil.fromJsonByFieldNamingPolicy(data, GitlabProject.class);
    }
}
