package org.socyno.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
public class SystemDataChartFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    @Attributes(title = "唯一标识符", readonly = true)
    private String uuid;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "图表标题")
    private String title;
    
    @Attributes(title = "SQL查询语句")
    private String querySql;
    
    @Attributes(title = "图表类型", type = FieldChartType.class)
    private String chartType;
    
    @Attributes(title = "绘图参数")
    private String chartParams;
    
    @Attributes(title = "求和方式", type = FieldOptionSum.class)
    private String dataSum;
    
    @Attributes(title = "SQL查询参数")
    private String sqlParams;
    
    @Attributes(title = "邮件模板")
    private String mailTemplate;
    
    @Attributes(title = "数据标签列")
    private String dataLabelColumn;
    
    @Getter
    public enum OptionSum {
        NO_SUM("noSum"   ,"无需求和"),
        XO_SUM("xoSum"   ,"横向求和"),
        YO_SUM("yoSum"   ,"纵向求和"),
        XY_SUM("xySum"   ,"横纵求和"),
        
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
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemDataChartService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    @Getter
    public enum ChartType {
        LINE("line", "线图"),
        BAR ("bar" , "柱图"),
        PIE ("pie" , "饼图"),
        NONE("none", "无图")
        ;
        private final String code;
        private final String display;

        ChartType(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }

    public static class FieldChartType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> OPTIONS = new ArrayList<FieldSimpleOption>() {
            {
                for (ChartType type : ChartType.values()) {
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
