package org.socyno.webfwk.state.model;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonFormAttachement extends CommonAttachementItem {
    
    @Attributes(title = "流程单名称", readonly = true)
    private String formName;
    
}
