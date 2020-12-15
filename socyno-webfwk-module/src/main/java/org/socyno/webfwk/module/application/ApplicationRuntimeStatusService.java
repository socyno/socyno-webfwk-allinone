package org.socyno.webfwk.module.application;

import java.util.List;

public abstract class ApplicationRuntimeStatusService {

    protected abstract List<ApplicationRuntimeStatusNodeItem> queryApplicationDeployInfo(String environment , String contextRoot , Long applicationId)
            throws Exception;

}
