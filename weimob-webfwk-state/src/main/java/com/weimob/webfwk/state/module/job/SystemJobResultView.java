package com.weimob.webfwk.state.module.job;

import com.weimob.webfwk.state.util.StateFormEventResultWebSocketViewLink;

import lombok.Getter;

@Getter
public class SystemJobResultView extends StateFormEventResultWebSocketViewLink {
    
    private final long taskId;

    public SystemJobResultView(long taskId, String url, String parameters) throws Exception {
        super(url, parameters);
        this.taskId = taskId;
    }
    
}