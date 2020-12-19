package org.socyno.webfwk.state.module.notify;

import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "通知模板详情")
public class SystemNotifyTemplateFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemNotifyTemplateService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "功能描述", type = FieldText.class)
    private String comment;
    
    @Attributes(title = "邮件收件人")
    private String mailTo;
    
    @Attributes(title = "邮件抄送人")
    private String mailCc;
    
    @Attributes(title = "移动手机号")
    private String messageTo;
    
    @Attributes(title = "邮件模板", type = FieldText.class)
    private String mailContent;
    
    @Attributes(title = "短消息模板", type = FieldText.class)
    private String messageContent;
    
}
