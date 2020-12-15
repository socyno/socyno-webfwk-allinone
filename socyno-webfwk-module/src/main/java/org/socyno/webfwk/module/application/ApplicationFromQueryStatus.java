package org.socyno.webfwk.module.application;

import lombok.Getter;
import lombok.Setter;

import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.state.basic.BasicStateForm;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
public class ApplicationFromQueryStatus extends BasicStateForm {

    @Attributes(title = "请选择环境", required = true, type = FieldDeployEnvironment.class)
    private String environment ;

}
