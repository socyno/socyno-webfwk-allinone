package com.weimob.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemDataChartFormMail extends StateFormBasicInput {

    @Attributes(title = "邮件模板代码")
    private String mailTemplate;
}
