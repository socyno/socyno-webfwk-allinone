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
public class GitlabUser {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String state;
    private String webUrl;
    private String createdAt;
    
    public static GitlabUser fromJson(@NonNull JsonElement data) throws IllegalJsonDataException, Exception {
        GitlabUser user;
        if ((user = CommonUtil.fromJsonByFieldNamingPolicy(data, GitlabUser.class)) == null || user.getId() == null
                || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getEmail())) {
            throw new IllegalJsonDataException();
        }
        return user;
    }
    
}
