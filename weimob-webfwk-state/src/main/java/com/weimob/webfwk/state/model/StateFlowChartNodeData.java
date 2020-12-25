package com.weimob.webfwk.state.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateChoice;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Getter
@Setter
@ToString
public class StateFlowChartNodeData {
    
    public enum Category {
        STATE,
        ACTION,
        CHOICE, 
        YESNO,
        UNCHANGED;
    }
    
    private final String key;
    
    private final String text;
    
    private final String name;
    
    private final boolean current;
    
    private final boolean next;
    
    private List<String> currentApprovalPeopleList;
    
    private Category category;
    
    public StateFlowChartNodeData(boolean yesNo, String keyPrefix) {
        category = Category.YESNO;
        key = String.format("%s:%s-%s", category, keyPrefix, yesNo);
        text = yesNo ? "是" : "否";
        name = String.format("%s", yesNo);
        next = false;
        current = false;
    }
    
    public StateFlowChartNodeData(String state, String display, AbstractStateFormBase stateRevision) {
        if (StringUtils.isBlank(state)) {
            category = Category.UNCHANGED;
            key = category.name();
            text = "状态保持不变";
            name = category.name();
            next = false;
            current = false;
            return;
        }
        next = false;
        name = state;
        category = Category.STATE;
        text = CommonUtil.ifNull(display, state);
        current = stateRevision != null && stateRevision.getState() != null
                && stateRevision.getState().equals(state);
        key = String.format("%s:%s", category, state);
    }
    
    public StateFlowChartNodeData(AbstractStateChoice choice) {
        category = Category.CHOICE;
        key = String.format("%s:%s", category, choice.getClass().getName());
        text = choice.getDisplay();
        name = choice.getClass().getName();
        next = false;
        current = false;
    }
    
    public StateFlowChartNodeData(String actionName, AbstractStateAction<?, ?, ?> action) {
        category = Category.ACTION;
        key = String.format("%s:%s", category, actionName);
        text = action.getDisplay();
        name = actionName;
        next = false;
        current = false;
    }
    
    public StateFlowChartNodeData(String keyVal, String textVal, String nameVal, List<String> currentApprovalPeoplesVal) {
        category = Category.ACTION;
        key = keyVal;
        text = textVal;
        name = nameVal;
        currentApprovalPeopleList = currentApprovalPeoplesVal;
        next = true;
        current = false;
    }
    
    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
    
    @Override
    public boolean equals(Object another) {
        if (another == null || !StateFlowChartNodeData.class.equals(another.getClass())) {
            return false;
        }
        return StringUtils.equals(((StateFlowChartNodeData) another).getKey(), key);
    }
}
