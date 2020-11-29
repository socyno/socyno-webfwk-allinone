package org.socyno.webfwk.module.app.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldTextLine;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationFormFromBranchRelease extends BasicStateForm {
    
    @Attributes(title = "分支路径", required = true, position = 1010, type = FieldTextLine.class)
    private String releaseBranch ;

}
