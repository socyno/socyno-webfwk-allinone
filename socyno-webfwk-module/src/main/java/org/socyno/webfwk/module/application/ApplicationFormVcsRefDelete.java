package org.socyno.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.module.vcs.common.FieldVcsRefsName;
import org.socyno.webfwk.state.basic.BasicStateForm;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class ApplicationFormVcsRefDelete extends BasicStateForm {

    @Attributes(title = "名称", required = true, type = FieldVcsRefsName.class)
    private String vcsRefsName;

}