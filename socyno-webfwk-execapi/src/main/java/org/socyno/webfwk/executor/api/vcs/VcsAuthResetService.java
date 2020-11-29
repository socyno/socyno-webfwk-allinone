package org.socyno.webfwk.executor.api.vcs;

import org.socyno.webfwk.executor.abs.AbstractJobExecutorService;
import org.socyno.webfwk.executor.abs.AbstractJobManager;
import org.socyno.webfwk.executor.model.JobBasicStatus;

import lombok.Getter;

public class VcsAuthResetService extends AbstractJobManager<VcsAuthResetParameters, JobBasicStatus> {
    
    public final static VcsAuthResetService DEFAULT = new VcsAuthResetService();
    
    @Getter
    public static enum Predefined {
        DEFAULT(VcsAuthResetService.DEFAULT);
        private final VcsAuthResetService service;
        
        Predefined(VcsAuthResetService service) {
            this.service = service;
        }
    }
    
    @Override
    protected String getTaskPath() {
        return "vcsAuthzReset";
    }
    
    @Override
    protected int getTimeoutSeconds() {
        return 60000 * 30;
    }
    
    @Override
    protected AbstractJobExecutorService getService() {
        return AbstractJobExecutorService.DEFAULT;
    }
    
    public static VcsAuthResetService getInstance(String name) {
        for (VcsAuthResetService.Predefined predefined : VcsAuthResetService.Predefined.values()) {
            if (predefined.name().equalsIgnoreCase(name)) {
                return predefined.getService();
            }
        }
        return null;
    }
}
