package org.socyno.webfwk.state.exec;

import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class StateFormFieldInvalidOptionException extends MessageException {

    private static final long serialVersionUID = 1L;

    private String value;
    private Class<? extends FieldOption> clazz;
    
    public StateFormFieldInvalidOptionException(@NonNull Class<? extends FieldOption> clazz, String value) {
        this.value = value;
        this.clazz = clazz;
    }
    
    @Override
    public String getMessage() {
        return String.format("表单字段的输入选项值不正确（option class=%s, option value=%s）.", 
                            clazz.getName(), CommonUtil.ifNull(value, ""));
    }
}
