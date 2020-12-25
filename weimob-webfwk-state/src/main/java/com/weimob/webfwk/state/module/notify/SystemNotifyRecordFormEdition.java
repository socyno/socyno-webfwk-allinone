package com.weimob.webfwk.state.module.notify;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormSimple.FieldOptionsType;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑通知记录")
public class SystemNotifyRecordFormEdition extends StateFormBasicInput {
    
    @Attributes(title = "类型", readonly = true, type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "收件人", description = "多个可使用逗号或分号分隔")
    private String messageTo;
    
    @Attributes(title = "抄送人", description = "多个可使用逗号或分号分隔")
    private String messageCc;
    
    @Attributes(title = "通知内容", required = true, type = FieldText.class)
    private String content;
}
