package com.weimob.webfwk.module.deploy.environment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

@Getter
@Setter
@ToString
public class DeployEnvironmentFormEdition extends StateFormBasicInput implements AbstractDeployEnvironmentForm {
    
    @Attributes(title = "代码", required = true)
    private String name;
    
    @Attributes(title = "名称", required = true)
    private String display;
    
}
