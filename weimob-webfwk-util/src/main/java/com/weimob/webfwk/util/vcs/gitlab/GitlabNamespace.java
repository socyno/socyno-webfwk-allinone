package com.weimob.webfwk.util.vcs.gitlab;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.google.gson.JsonElement;

@Getter
@Setter
@ToString
public class GitlabNamespace extends GitlabAbsEntity {
    private Long id;
    private String name;
    private String path;
    private String kind;
    private Long parentId;
    private String fullPath;
    

    @Override
    protected String[] getRequiredFields() {
        return new String[] { "id", "name", "path", "kind", "full_path" };
    }

    @Override
    protected String[] getOptionalFields() {
        return new String[] {"parent_id"};
    }

    @Override
    protected String[] getLongFields() {
        return new String[] { "id", "parent_id" };
    }

    public static GitlabNamespace fromJson(JsonElement data) throws IllegalJsonDataException, Exception {
        return new GitlabNamespace().fromJson(data, GitlabNamespace.class);
    }

}
