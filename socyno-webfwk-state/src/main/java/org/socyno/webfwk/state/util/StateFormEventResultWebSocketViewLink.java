package org.socyno.webfwk.state.util;

import org.socyno.webfwk.state.basic.AbstractStateFormEventResultView;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Attributes(title = "通用WebSocket视图，事件响应该类型数据则执行打开新窗口显示指定WebSocket视图")
public class StateFormEventResultWebSocketViewLink implements AbstractStateFormEventResultView {

    private final String eventResultViewType = "WebSocketViewLink";
    
    private final String url;
    
    private final String parameters; 
    
    public StateFormEventResultWebSocketViewLink(String url, String parameters) throws Exception {
        this.url = url;
        this.parameters = parameters;
    }
}
