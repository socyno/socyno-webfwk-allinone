package com.weimob.webfwk.state.util;

import java.util.Date;

import com.weimob.webfwk.state.abs.AbstractStateFormBase;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFormRevision implements AbstractStateFormBase {
    
    private Long id;
    
    private String state;
    
    private Long revision;
    
    private Date updatedAt;
    
    private Long updatedBy;
    
    private String updatedCodeBy;
    
    private String updatedNameBy;
    
    private Date createdAt;
    
    private Long createdBy;
    
    private String createdCodeBy;
    
    private String createdNameBy;
}
