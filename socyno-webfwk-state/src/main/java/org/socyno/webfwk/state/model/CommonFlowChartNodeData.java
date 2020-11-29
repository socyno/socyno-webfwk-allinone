package org.socyno.webfwk.state.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateChoice;
import org.socyno.webfwk.state.util.StateFormRevision;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

@Getter
@Setter
@ToString
public class CommonFlowChartNodeData {

    public enum Category {
        STATE,
        ACTION,
        CHOICE,
        YESNO,
        STATE_CURRENT,
        NEXT_ACTION,
        UNCHANGED;
    }

    private final String key;

    private final String text;

    private final String name;

    private final boolean current;

    private List<String> currentApprovalPeopleList;

    private Category category;

    public CommonFlowChartNodeData(boolean yesNo, String keyPrefix) {
        category = Category.YESNO;
        key = String.format("%s:%s-%s", category, keyPrefix, yesNo);
        text = yesNo ? "是" : "否";
        name = String.format("%s", yesNo);
        current = false;
    }
    
    public CommonFlowChartNodeData(String state, String display, StateFormRevision stateRevision) {
        if (StringUtils.isBlank(state)) {
            category = Category.UNCHANGED;
            key = category.name();
            text = "状态保持不变";
            name = category.name();
            current = false;
            return;
        }
        name = state;
        text = CommonUtil.ifNull(display, state);
        current = stateRevision != null && stateRevision.getStateFormStatus() != null
                        && stateRevision.getStateFormStatus().equals(state);
        category = current ? Category.STATE_CURRENT : Category.STATE;
        key = String.format("%s:%s", category, state);
    }

    public CommonFlowChartNodeData(AbstractStateChoice choice) {
        category = Category.CHOICE;
        key = String.format("%s:%s", category, choice.getClass().getName());
        text = choice.getDisplay();
        name = choice.getClass().getName();
        current = false;
    }
    
    public CommonFlowChartNodeData(String actionName, AbstractStateAction<?, ?, ?> action) {
        category = Category.ACTION;
        key = String.format("%s:%s", category, actionName);
        text = action.getDisplay();
        name = actionName;
        current = false;
    }

    public CommonFlowChartNodeData(String keyVal, String textVal, String nameVal, List<String> currentApprovalPeoplesVal) {
        category = Category.NEXT_ACTION;
        key = keyVal;
        text = textVal;
        name = nameVal;
        currentApprovalPeopleList = currentApprovalPeoplesVal;
        current = false;
    }
    
    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null || !CommonFlowChartNodeData.class.equals(another.getClass())) {
            return false;
        }
        return StringUtils.equals(((CommonFlowChartNodeData) another).getKey(), key);
    }
}
