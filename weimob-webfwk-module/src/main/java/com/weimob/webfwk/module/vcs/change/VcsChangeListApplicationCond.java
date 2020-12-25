package com.weimob.webfwk.module.vcs.change;

import lombok.Data;

@Data
public class VcsChangeListApplicationCond  {
    
    private String vcsRefsName;
    
    private String vcsRevision;
    
    private Long createdBy;
}
