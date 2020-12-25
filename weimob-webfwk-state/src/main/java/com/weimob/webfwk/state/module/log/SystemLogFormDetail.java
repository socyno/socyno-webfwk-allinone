package com.weimob.webfwk.state.module.log;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemLogFormDetail extends SystemLogFormDefault implements SystemLogFormWithDetails {

    @Attributes(title = "操作前内容")
    private String operateBefore;

    @Attributes(title = "操作后内容")
    private String operateAfter;

}
