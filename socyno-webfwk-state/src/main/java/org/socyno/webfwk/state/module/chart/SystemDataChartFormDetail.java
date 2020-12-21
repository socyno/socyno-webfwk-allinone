package org.socyno.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import org.socyno.webfwk.util.state.field.FieldTextHtml;

@Getter
@Setter
@ToString
public class SystemDataChartFormDetail extends SystemDataChartFormDefault {
    
    @Attributes(title = "图表展示")
    private List<Map<String, Object>> chartData;
    
    @Attributes(title = "SQL参数")
    private Map<String, String> sqlParamsMap;
    
    @Attributes(title = "邮件内容", type = FieldTextHtml.class, readonly = true)
    private String mailContent;

    @Attributes(title = "异常信息")
    private String errorMessage;
}
