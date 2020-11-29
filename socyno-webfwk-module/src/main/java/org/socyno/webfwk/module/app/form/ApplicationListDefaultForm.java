package org.socyno.webfwk.module.app.form;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ApplicationListDefaultForm extends ApplicationFormSimple implements ApplicationWithNamespaceSummaries {

    @Attributes(title = "部署机组概要")
    private List<DeployEnvNamespaceSummarySimple> deployNamespaceSummaries;
    
}
