package com.weimob.webfwk.state.util;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormEventResultView;
import com.weimob.webfwk.util.tool.ClassUtil;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Attributes(title = "标准表单展示视图，事件响应该类型数据则弹框显示表单内容")
public class StateFormEventResultSimpleView implements AbstractStateFormEventResultView {

    private final String eventResultViewType = "SimpleView";
    
    private final String formClass;
    
    public StateFormEventResultSimpleView() throws Exception {
        this.formClass = ClassUtil.classToJson(this.getClass()).toString();
    }
}
