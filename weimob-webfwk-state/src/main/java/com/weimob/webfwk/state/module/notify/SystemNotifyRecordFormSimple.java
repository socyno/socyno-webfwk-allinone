package com.weimob.webfwk.state.module.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.util.StateFormBasicSaved;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "通知记录内容")
public class SystemNotifyRecordFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    @Getter
    public enum MessageType {
        Email("email", "邮件"),
        Message("message", "短消息"),
        Weixin("weixin", "企业微信")
        ;
        private final String value;
        
        private final String display;
        
        MessageType (String value, String display) {
            this.value = value;
            this.display = display;
        }
    }
    
    @Getter
    public enum SendResult {
        Skipped("skipped", "忽略"),
        Success("success", "成功"),
        Failure("failure", "失败"),
        Partial("partical", "部分成功")
        ;
        private final String value;
        
        private final String display;
        
        SendResult (String value, String display) {
            this.value = value;
            this.display = display;
        }
    }
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemNotifyRecordService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    public static class FieldOptionsResult extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {{
            for (SendResult type : SendResult.values()) {
                add(FieldSimpleOption.create(type.getValue(),  type.getDisplay()));
            }
        }};
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }

    public static class FieldOptionsType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {{
            for (MessageType type : MessageType.values()) {
                add(FieldSimpleOption.create(type.getValue(),  type.getDisplay()));
            }
        }};
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "类型", type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "通知内容", type = FieldText.class)
    private String content;
    
    @Attributes(title = "收件人", description = "多个可使用逗号或分号分隔")
    private String messageTo;
    
    @Attributes(title = "抄送人", description = "多个可使用逗号或分号分隔")
    private String messageCc;
    
    @Attributes(title = "发送结果", type = FieldOptionsResult.class)
    private String result;
}
