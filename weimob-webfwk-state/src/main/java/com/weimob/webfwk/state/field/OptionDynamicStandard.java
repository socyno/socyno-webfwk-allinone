package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.v1.FieldSimpleOption;

import com.google.gson.reflect.TypeToken;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class OptionDynamicStandard extends FieldSimpleOption {
    
    public OptionDynamicStandard() {
        super(null);
    }

    public OptionDynamicStandard(String value) {
        super(value);
        setOptionValue(value);
    }
    
    private String classPath;
    
    private String category ;
    
    private String properties;
    
    private boolean disabled;
    
    public static Map<String, String> parseProperties(String properties, Map<String, String> dfltData) {
        if (StringUtils.isBlank(properties)) {
            return Collections.emptyMap();
        }
        try {
            return CommonUtil.fromJson(properties, new TypeToken<Map<String, String>>() {}.getType());
        } catch(Exception ex) {
            return dfltData;
        }
    }
    
    public Map<String, String> getPropertiesMap() {
        return parseProperties(properties, Collections.emptyMap());
    }
    
    public boolean equals(Object another) {
        if (another == null || !another.getClass().equals(getClass())) {
            return false;
        }
        return StringUtils.equals(getOptionValue(), ((OptionDynamicStandard) another).getOptionValue())
                && StringUtils.equals(getClassPath(), ((OptionDynamicStandard) another).getClassPath())
                && StringUtils.equals(getCategory(), ((OptionDynamicStandard) another).getCategory());
    }
    
    public int hashCode() {
        return String.format("%s-%s-%s", getClassPath(), getOptionValue() , getCategory()).hashCode();
    }
}
