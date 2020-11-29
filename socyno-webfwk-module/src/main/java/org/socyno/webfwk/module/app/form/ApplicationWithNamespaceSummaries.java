package org.socyno.webfwk.module.app.form;

import java.util.List;

public interface ApplicationWithNamespaceSummaries {

    public List<DeployEnvNamespaceSummarySimple> getDeployNamespaceSummaries();

    public void setDeployNamespaceSummaries(List<DeployEnvNamespaceSummarySimple> deployNamespaceSummaries);

}
