package org.socyno.webfwk.state.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@ToString
public class StateFlowChartDefinition {
    
    private Collection<StateFlowChartNodeData> nodeData;
    
    private List<StateFlowChartLinkData> linkData;
    
    public StateFlowChartDefinition(Collection<StateFlowChartNodeData> nodeData,
            Collection<StateFlowChartLinkData> linkData) {
        if (nodeData != null) {
            this.nodeData = new HashSet<>(nodeData);
        }
        if (linkData != null) {
            this.linkData = new ArrayList<>(linkData);
        }
    }
}
