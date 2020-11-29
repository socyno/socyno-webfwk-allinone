package org.socyno.webfwk.util.vcs.svn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@Accessors(chain=true)
public class SubversionMaiaGroupPermission {
    
    private String groupId;
    
    private String groupName;
    
    private String permission;
}

