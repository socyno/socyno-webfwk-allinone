package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.module.app.form.DeployEnvNamespaceSummarySimple;
import org.socyno.webfwk.util.tool.CommonUtil;

@Data
public class SubsystemApplicationSummary {
    
    @Setter(AccessLevel.NONE)
    @Attributes(title = "应用总数")
    private int appTotal;
    
    @Attributes(title = "应用类型概要信息")
    private final List<ApptypeSummary> typedAppSummary = new ArrayList<>();;
    
    @Attributes(title = "业务系统环境统计")
    private final List<DeployEnvNamespaceSummarySimple> evnNamespaceSummary = new ArrayList<>();;
    
    @Data
    @Accessors(chain = true)
    public static class ApptypeSummary {
        @Attributes(title = "应用类型")
        private String type;
        
        @Attributes(title = "类型显示")
        private String display;
        
        @Attributes(title = "类型总数")
        private Integer total;
        
    }
    
    public void addTypedAppSummary(String type, String display, int total) {
        appTotal = CommonUtil.ifNull(appTotal, 0) + total;
        typedAppSummary.add(new ApptypeSummary().setType(type).setDisplay(display).setTotal(total));
    }
}