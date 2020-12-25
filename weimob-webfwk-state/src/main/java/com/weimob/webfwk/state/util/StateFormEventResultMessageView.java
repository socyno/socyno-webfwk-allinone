package com.weimob.webfwk.state.util;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormEventMessageAppender;
import com.weimob.webfwk.state.abs.AbstractStateFormEventResultView;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Attributes(title = "结果消息显示视图，即将结果的消息显示在页面上")
public class StateFormEventResultMessageView implements AbstractStateFormEventResultView, AbstractStateFormEventMessageAppender {

    private final String eventResultViewType = "MessageView";
    
    private String message;
    
    private String eventAppendMessage;
    
    public StateFormEventResultMessageView() {
        
    }
    
    public StateFormEventResultMessageView(String message) {
        this.message = message;
    }
}
