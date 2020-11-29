package org.socyno.webfwk.module.app.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.module.vcs.common.FieldVcsRefsName;
import org.socyno.webfwk.state.basic.BasicStateForm;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationVcsRefsCreate extends BasicStateForm {
    
    @Attributes(title = "名称", required = true, position = 10)
    private String vcsRefsName;
    
    @Attributes(title = "基线版本", required = true, type = FieldVcsRefsName.class, position = 20)
    private String refOrCommit;

}
