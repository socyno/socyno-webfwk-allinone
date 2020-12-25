package com.weimob.webfwk.state.util;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateCreateView;

import lombok.Getter;

@Getter
@Attributes(title = "表单创建事件响应基础视图")
public class StateFormEventResultCreateViewBasic implements AbstractStateCreateView {
    private final long id;
    
    public StateFormEventResultCreateViewBasic(long id) {
        this.id = id;
    }
}
