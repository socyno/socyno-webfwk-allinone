package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.field.AbstractFieldDynamicStandard;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.state.util.StateFormBasicForm;

@Getter
@Setter
@ToString
@Attributes(title = "图表新增",
        visibleSelector = {"event_chart_create"},
        requiredSelector = {"event_chart_create"},
        editableSelector = {"event_chart_create"})
public class DataChartFormCreation extends StateFormBasicForm {
    
    @Attributes(title = "图表标题",             
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"},
            requiredTags= {"event_chart_create", "event_chart_edit"})
    private String title;

    @Attributes(title = "SQL语句",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"},
            requiredTags= {"event_chart_create", "event_chart_edit"})
    private String querySql;
    
    @Attributes(title = "图表类型",            
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create"},
            requiredTags= {"event_chart_create"}, type = FieldChartType.class)
    private OptionDynamicStandard chartType;

    @Attributes(title = "求和选择",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"}, type = FieldOptionSum.class)
    private String needSum;

    @Attributes(title = "sql参数",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String sqlParams;

    @Attributes(title = "绘图参数",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String chartParams;

    @Attributes(title = "邮件模板code",
            visibleTags = {"event_chart_create", "event_chart_edit"},
            editableTags = {"event_chart_create", "event_chart_edit"})
    private String mailCode;

    @Attributes(title = "指定第一列",
            visibleTags= {"event_chart_create", "event_chart_edit"},
            editableTags= {"event_chart_create", "event_chart_edit"})
    private String firstColumn;
    
    public static class FieldChartType extends AbstractFieldDynamicStandard { }

    @Getter
    public enum OptionSum{
        NO_SUM("noSum","无需求和"),
        X_SUM("xSum","横向求和"),
        Y_SUM("ySum","纵向求和"),
        X_Y_SUM("xySum","横纵求和"),

        ;
        private final String code;
        private final String display;

        OptionSum(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }

    public static class FieldOptionSum extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> OPTIONS = new ArrayList<FieldSimpleOption>() {
            {
                for (OptionSum type : OptionSum.values()) {
                    add(FieldSimpleOption.create(type.getCode(),type.getDisplay()));
                }
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(OPTIONS);
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

}
