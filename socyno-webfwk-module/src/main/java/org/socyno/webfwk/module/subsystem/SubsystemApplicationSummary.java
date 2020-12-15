package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.module.application.DeployEnvNamespaceSummarySimple;

@Data
public class SubsystemApplicationSummary {
    
    @Attributes(title = "应用总数")
    private int total;
    
    @Attributes(title = "应用类型概要")
    private final List<AppTypeSummary> types = new ArrayList<>();;
    
    @Attributes(title = "部署环境概要")
    private final List<DeployEnvNamespaceSummarySimple> envs = new ArrayList<>();;
    
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class AppTypeSummary {
        @Attributes(title = "应用类型")
        private String type;
        
        @Attributes(title = "类型显示")
        private String display;
        
        @Attributes(title = "应用总数")
        private int total = 0;
        
        @Attributes(title = "应用状态概要")
        private final List<AppStateSummary> states = new ArrayList<>();
        
        public void addStateSummary(String code, String display, int total) {
            states.add(new AppStateSummary().setCode(code).setDisplay(display).setTotal(total));
        }
    }
    
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class AppStateSummary {
        @Attributes(title = "应用环境")
        private String code;
        
        @Attributes(title = "环境名称")
        private String display;
        
        @Attributes(title = "应用总数")
        private int total = 0;
    }
}