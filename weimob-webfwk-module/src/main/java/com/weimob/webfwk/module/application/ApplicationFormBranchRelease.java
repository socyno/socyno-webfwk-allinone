package com.weimob.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldTextLine;

@Getter
@Setter
@ToString
public class ApplicationFormBranchRelease extends StateFormBasicInput {
    
    @Attributes(title = "分支路径", required = true, type = FieldTextLine.class)
    private String releaseBranch ;

}
