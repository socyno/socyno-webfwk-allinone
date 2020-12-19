package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
public class DataChartFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {

    @Attributes(title = "状态", readonly = true, type = DataChartFormDetail.FieldOptionsState.class)
    private String state;

    @Attributes(title = "图表标题")
    private String title;

    @Attributes(title = "SQL语句")
    private String querySql;

    @Attributes(title = "图表类型" , type = DataChartFormCreation.FieldChartType.class)
    private OptionDynamicStandard chartType;

    @Attributes(title = "绘图参数")
    private String chartParams;

    @Attributes(title = "求和选择", type = DataChartFormCreation.FieldOptionSum.class)
    private String needSum;

    @Attributes(title = "sql参数")
    private String sqlParams;

    @Attributes(title = "uuid")
    private String uuid;

    @Attributes(title = "邮件模板code")
    private String mailCode;

    @Attributes(title = "指定第一列")
    private String firstColumn;

}
