package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OptionSystemAuth implements FieldOption {
    
    @Getter
    public static enum AuthType {
        Interfaze("interface", "系统接口"),
        FormEvent("form_event", "表单事件");
        
        private final String code;
        
        private final String display;
        
        AuthType(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }
    
    public static class FieldOptionsAuthType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (AuthType type : AuthType.values()) {
                    add(FieldSimpleOption.create(type.getCode(), type.getDisplay()));
                }
            }
        };
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "授权范围", position = 10)
    private String scopeType;
    
    @Attributes(title = "授权标识", position = 20, type = FieldOptionsAuthType.class)
    private String type;
    
    @Attributes(title = "授权标识", position = 30)
    private String auth;
    
    @Attributes(title = "请求方式")
    private String requestMethods;
    
    @Attributes(title = "请求函数")
    private String controllerMethod;
    
    @Override
    public String getOptionValue() {
        return getAuth();
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s:%s", getScopeType(), getAuth());
    }
    
    @Override
    public String getOptionGroup() {
        return type;
    }
    
    @Override
    public void setOptionValue(String value) {
        setAuth(value);
    }
}
