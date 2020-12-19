package org.socyno.webfwk.module.deploy.environment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.util.StateFormBasicForm;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
public class DeployEnvironmentFormEdition extends StateFormBasicForm implements AbstractDeployEnvironmentForm {
    
    @Attributes(title = "代码", required = true)
    private String name;
    
    @Attributes(title = "名称", required = true)
    private String display;
    
}
