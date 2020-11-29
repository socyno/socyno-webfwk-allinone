package org.socyno.webfwk.state.module.notify;

import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldText;
import org.socyno.webfwk.state.module.notify.SystemNotifyRecordSimple.FieldOptionsType;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑通知记录")
public class SystemNotifyRecordForEdition extends BasicStateForm {
    
    @Attributes(title = "类型", readonly = true, type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "收件人", description = "多个可使用逗号或分号分隔")
    private String messageTo;
    
    @Attributes(title = "抄送人", description = "多个可使用逗号或分号分隔")
    private String messageCc;
    
    @Attributes(title = "通知内容", required = true, type = FieldText.class)
    private String content;
}
