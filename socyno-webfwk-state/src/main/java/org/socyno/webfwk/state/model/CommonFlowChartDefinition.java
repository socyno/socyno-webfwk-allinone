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
public class CommonFlowChartDefinition {
    private Collection<CommonFlowChartNodeData> nodeData;

    private List<CommonFlowChartLinkData> linkData;

    public CommonFlowChartDefinition (Collection<CommonFlowChartNodeData> nodeData, List<CommonFlowChartLinkData> linkData){
        this.nodeData = nodeData == null ? nodeData : new HashSet<>(nodeData);
        this.linkData = linkData == null ? linkData : new ArrayList<>(linkData);
    }
}
