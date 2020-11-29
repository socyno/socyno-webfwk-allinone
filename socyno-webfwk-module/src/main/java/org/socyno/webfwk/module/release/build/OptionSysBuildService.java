package org.socyno.webfwk.module.release.build;

import lombok.Data;

import org.socyno.webfwk.state.basic.AbstractStateForm;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Data
public class OptionSysBuildService implements AbstractStateForm, FieldOption {
    
    @Attributes(title = "编号", position = 10)
    private Long id;
    
    @Attributes(title = "状态", position = 20)
    private String state;
    
    @Attributes(title = "版本", position = 50)
    private Long revision;
    
    @Attributes(title = "代码", position = 30)
    private String code;
    
    @Attributes(title = "标题", position = 40)
    private String title;
    
    @Override
    public String getOptionValue() {
        return getCode();
    }
    
    @Override
    public void setOptionValue(String value) {
        setCode(value);
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s-%s", getCode(), getTitle());
    }
}
