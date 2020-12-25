package com.weimob.webfwk.util.vcs.gitlab;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class GitlabGroupMember {
    private long id;
    private String name;
    private String username;
    private String state;
    private String avatarUrl;
    private String webUrl;
    private int accessLevel;
    private String expiresAt;
}
