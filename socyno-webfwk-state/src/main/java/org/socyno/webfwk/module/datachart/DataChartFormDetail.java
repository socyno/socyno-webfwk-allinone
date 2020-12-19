package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import org.socyno.webfwk.util.state.field.FieldTextHtml;

@Getter
@Setter
@ToString
public class DataChartFormDetail extends DataChartFormDefault {

    @Attributes(title = "图表展示")
    private List<Map<String,Object>> chartData;
    
    @Attributes(title = "sql参数")
    private Map<String,String> sqlParamsMap;

    @Attributes(title = "邮件模板生成表格", type = FieldTextHtml.class, readonly = true)
    private String mailTable;

    @Attributes(title = "邮件模板数据")
    private List<Map<String,Object>> mailContext;
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return DataChartService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
}
