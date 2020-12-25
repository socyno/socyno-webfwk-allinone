package com.weimob.webfwk.util.vcs.gitlab;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.google.gson.JsonElement;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabBranch {
    private String name;

    public static GitlabBranch fromJson(@NonNull JsonElement data) throws IllegalJsonDataException, Exception {

        GitlabBranch branch ;
        if ((branch = CommonUtil.fromJsonByFieldNamingPolicy(data, GitlabBranch.class)) == null
                || StringUtils.isBlank(branch.getName())) {
            throw new IllegalJsonDataException();
        }
        return branch;
    }
}
