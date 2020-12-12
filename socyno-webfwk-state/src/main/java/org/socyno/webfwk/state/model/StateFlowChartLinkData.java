package org.socyno.webfwk.state.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFlowChartLinkData {
    
    private String to;
    
    private String from;
    
    private String linkText;
    
    private Category category;
    
    public enum Category {
        LINK_CURRENT,
        LINK_OTHER;
    }
    
    public StateFlowChartLinkData(String from, String to, boolean isLinkCurrent) {
        this.from = from;
        this.to = to;
        this.category = isLinkCurrent ? Category.LINK_CURRENT : Category.LINK_OTHER;
    }
}
