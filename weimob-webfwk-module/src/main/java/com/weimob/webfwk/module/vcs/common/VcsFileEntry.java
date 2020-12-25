package com.weimob.webfwk.module.vcs.common;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsFileEntry {
    private String  name;
    
    private Boolean isDir;
    
    private String  path;
    
    private String  size;
    
    private String  revision;
    
    private String  commiter;
    
    private String  message;
    
    private Date   lastUpdated;
}
