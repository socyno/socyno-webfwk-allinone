package com.weimob.webfwk.state.util;
import java.util.Date;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFormBasicSaved extends StateFormBasicInput {
    
    @Attributes(title = "创建时间", readonly = true)
    private Date createdAt;
    
    @Attributes(title = "创建人编号", readonly = true)
    private Long createdBy;
    
    @Attributes(title = "创建人账户", readonly = true)
    private String createdCodeBy;
    
    @Attributes(title = "创建人", readonly = true)
    private String createdNameBy;
    
    @Attributes(title = "更新时间", readonly = true)
    private Date updatedAt;
    
    @Attributes(title = "更新人编号", readonly = true)
    private Long updatedBy;
    
    @Attributes(title = "更新人账户", readonly = true)
    private String updatedCodeBy;
    
    @Attributes(title = "更新人", readonly = true)
    private String updatedNameBy;
}