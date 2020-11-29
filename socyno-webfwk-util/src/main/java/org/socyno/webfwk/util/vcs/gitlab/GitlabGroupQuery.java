package org.socyno.webfwk.util.vcs.gitlab;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabGroupQuery {
    private long id;
    private String webUrl;
    private String name;
    private String path;
    private String description;
    private String visibility;
    private boolean shareWithGroupLock;
    private boolean requireTwoFactorAuthentication;
    private int twoFactorGracePeriod;
    private String projectCreationLevel;
    private String autoDevopsEnabled;
    private String subgroupCreationLevel;
    private String emailsDisabled;
    private boolean lfsEnabled;
    private String avatarUrl;
    private boolean requestAccessEnabled;
    private String fullName;
    private String fullPath;
    private String parentId;
    private String ldapCn;
    private String ldapAccess;
}
