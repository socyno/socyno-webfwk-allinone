package org.socyno.webfwk.module.application;

import java.util.List;

public interface ApplicationWithNamespaceSummaries {

    public List<DeployEnvNamespaceSummarySimple> getDeployNamespaceSummaries();

    public void setDeployNamespaceSummaries(List<DeployEnvNamespaceSummarySimple> deployNamespaceSummaries);

}
