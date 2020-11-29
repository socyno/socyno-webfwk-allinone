package org.socyno.webfwk.executor.model;

import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Getter
public class JobStatusWebsocketLink {
    
    private final String parameters;
    
    private final String websocketUrl;
    
    public JobStatusWebsocketLink(long taskId, String websocketUrl) {
        
        this.websocketUrl = websocketUrl;
        this.parameters = CommonUtil.toJson(new ObjectMap().put("action", "Status")
                .put("parameters", new ObjectMap().put("taskId", taskId).asMap()).asMap());
    }
}
