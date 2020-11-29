package org.socyno.webfwk.module.app.form;

import java.util.List;

public abstract class ApplicationRuntimeStatusService {

    protected abstract List<ApplicationRuntimeStatusNodeItem> queryApplicationDeployInfo(String environment , String contextRoot , Long applicationId)
            throws Exception;

}
