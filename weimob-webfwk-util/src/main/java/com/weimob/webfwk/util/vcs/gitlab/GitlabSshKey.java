package com.weimob.webfwk.util.vcs.gitlab;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.google.gson.JsonElement;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabSshKey extends GitlabAbsEntity {
    private Long id;
    private String title;
    private String key;

    @Override
    protected String[] getRequiredFields() {
        return new String[] { "id", "title", "key" };
    }

    @Override
    protected String[] getOptionalFields() {
        return null;
    }

    @Override
    protected String[] getLongFields() {
        return new String[] { "id" };
    }

    public static GitlabSshKey fromJson(JsonElement data) throws IllegalJsonDataException, Exception {
        return new GitlabSshKey().fromJson(data, GitlabSshKey.class);
    }
}
