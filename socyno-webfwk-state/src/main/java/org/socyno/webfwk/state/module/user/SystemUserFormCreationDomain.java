package org.socyno.webfwk.state.module.user;

import org.socyno.webfwk.state.basic.AbstractStateForm;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加域用户")
public class SystemUserFormCreationDomain implements AbstractStateForm {
    
    @Attributes(title = "编号", readonly = true)
    private Long  id;
    
    @Attributes(title = "状态", readonly = true)
    private String  state;

    @Attributes(title = "版本", readonly = true)
    private Long  revision;

    @Attributes(title = "用户", position = 1010, required = true)
    private String  username;
}
