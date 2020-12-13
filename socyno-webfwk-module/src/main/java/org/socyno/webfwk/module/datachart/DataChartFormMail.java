package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import org.socyno.webfwk.state.basic.BasicStateForm;

@Getter
@Setter
@ToString
public class DataChartFormMail extends BasicStateForm {

    @Attributes(title = "邮件模板代码", readonly = true)
    private String mailCode;

    @Attributes(title = "邮件模板数据", readonly = true)
    private List<Map<String,Object>> mailContext;
    
}
