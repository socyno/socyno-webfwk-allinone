package org.socyno.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.module.chart.SystemDataChartFormSimple.FieldChartType;
import org.socyno.webfwk.state.module.chart.SystemDataChartFormSimple.FieldOptionSum;
import org.socyno.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
@Attributes(title = "图表新增",
        visibleSelector = {"event_chart_create"},
        requiredSelector = {"event_chart_create"},
        editableSelector = {"event_chart_create"})
public class SystemDataChartFormCreation extends StateFormBasicInput {
    
    @Attributes(title = "图表标题",             
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"},
            requiredTags= {"event_chart_create", "event_chart_edit"})
    private String title;

    @Attributes(title = "SQL查询语句",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"},
            requiredTags= {"event_chart_create", "event_chart_edit"})
    private String querySql;
    
    @Attributes(title = "图表类型",            
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create"},
            requiredTags= {"event_chart_create"},
            type = FieldChartType.class)
    private String chartType;

    @Attributes(title = "求和方式",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"},
            type = FieldOptionSum.class)
    private String dataSum;

    @Attributes(title = "SQL查询参数",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String sqlParams;

    @Attributes(title = "绘图参数",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String chartParams;

    @Attributes(title = "邮件模板",
            visibleTags = {"event_chart_create", "event_chart_edit"},
            editableTags = {"event_chart_create", "event_chart_edit"})
    private String mailTemplate;

    @Attributes(title = "数据标签列",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String dataLabelColumn;

    
}
