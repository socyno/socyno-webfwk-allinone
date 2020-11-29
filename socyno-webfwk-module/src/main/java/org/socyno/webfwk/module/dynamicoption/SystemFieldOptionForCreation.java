package org.socyno.webfwk.module.dynamicoption;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.basic.BasicStateForm;

@Getter
@Setter
@ToString
public class SystemFieldOptionForCreation extends BasicStateForm {
    
    @Attributes(title = "路径/名称", required = true)
    private String classPath;
    
    @Attributes(title = "选项清单", required = true, type = FieldSystemFieldOptionEntityCreate.class)
    private List<SystemFieldOptionEntity> options;    
    
}
