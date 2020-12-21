package org.socyno.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
public class SystemDataChartFormMail extends StateFormBasicInput {

    @Attributes(title = "邮件模板代码")
    private String mailTemplate;
}
