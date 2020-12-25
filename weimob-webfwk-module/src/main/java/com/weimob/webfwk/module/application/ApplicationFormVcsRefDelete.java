package com.weimob.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.vcs.common.FieldVcsRefsName;
import com.weimob.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
public class ApplicationFormVcsRefDelete extends StateFormBasicInput {

    @Attributes(title = "名称", required = true, type = FieldVcsRefsName.class)
    private String vcsRefsName;

}
