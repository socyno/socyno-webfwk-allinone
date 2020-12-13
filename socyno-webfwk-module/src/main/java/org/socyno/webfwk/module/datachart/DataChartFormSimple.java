package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.util.state.field.FieldDateTime;

@Getter
@Setter
@ToString
public class DataChartFormSimple implements AbstractStateForm {

    @Attributes(title = "编号")
    private Long id;

    @Attributes(title = "版本")
    private Long revision;

    @Attributes(title = "状态", type = DataChartFormDetail.FieldOptionsState.class)
    private String state;

    @Attributes(title = "申请人姓名")
    private String createdNameBy;

    @Attributes(title = "申请时间", type = FieldDateTime.class)
    private Date createdAt;

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
