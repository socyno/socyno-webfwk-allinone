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
