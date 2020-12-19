package org.socyno.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.util.state.field.FieldTextLine;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationFormBranchRelease extends StateFormBasicInput {
    
    @Attributes(title = "分支路径", required = true, type = FieldTextLine.class)
    private String releaseBranch ;

}
