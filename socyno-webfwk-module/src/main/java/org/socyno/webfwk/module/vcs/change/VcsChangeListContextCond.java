package org.socyno.webfwk.module.vcs.change;

import lombok.Data;

@Data
public class VcsChangeListContextCond  {

    private Long applicationId;
    
    private String vcsRefsName;
    
    private String vcsRevision;
}
