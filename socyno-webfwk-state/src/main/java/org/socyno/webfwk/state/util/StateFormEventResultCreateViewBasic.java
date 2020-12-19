package org.socyno.webfwk.state.util;

import org.socyno.webfwk.state.abs.AbstractStateCreateView;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

@Getter
@Attributes(title = "表单创建事件响应基础视图")
public class StateFormEventResultCreateViewBasic implements AbstractStateCreateView {
    private final long id;
    
    public StateFormEventResultCreateViewBasic(long id) {
        this.id = id;
    }
}
