package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "图表编辑",
        visibleSelector = {"event_chart_edit"},
        requiredSelector = {"event_chart_edit"},
        editableSelector = {"event_chart_edit"})
public class DataChartFormEdition extends DataChartFormCreation {
    
    
}