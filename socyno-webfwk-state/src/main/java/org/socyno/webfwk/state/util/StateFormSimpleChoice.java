package org.socyno.webfwk.state.util;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.basic.AbstractStateChoice;
import org.socyno.webfwk.state.basic.AbstractStateForm;

public class StateFormSimpleChoice extends AbstractStateChoice {
    
    protected StateFormSimpleChoice(String trueState) {
        super(trueState);
    }
    
    public static StateFormSimpleChoice getInstance(String trueState) {
        if (StringUtils.isBlank(trueState)) {
            return null;
        }
        return new StateFormSimpleChoice(trueState);
    }
    
    @Override
    public boolean isSimple() {
        return true;
    }
    
    @Override
    protected boolean select(AbstractStateForm form) {
        return true;
    }
}
