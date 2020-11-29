package org.socyno.webfwk.executor.api.build;

import org.socyno.webfwk.executor.abs.AbstractJobExecutorService;
import org.socyno.webfwk.executor.abs.AbstractJobManager;

import lombok.Getter;

public class ApplicationBuildService extends AbstractJobManager<ApplicationBuildParameters, ApplicationBuildStatus> {
    
    public final static ApplicationBuildService DEFAULT = new ApplicationBuildService();
    
    @Getter
    public static enum Predefined {
        DEFAULT(ApplicationBuildService.DEFAULT)
        ;
        private final ApplicationBuildService service;
        
        Predefined(ApplicationBuildService service) {
            this.service = service;
        }
    }
    
    @Override
    protected String getTaskPath() {
        return "build";
    }
    
    @Override
    protected AbstractJobExecutorService getService() {
        return AbstractJobExecutorService.DEFAULT;
    }
    
    @Override
    protected int getTimeoutSeconds() {
        return 60000 * 30;
    }
    
    public static ApplicationBuildService getInstance(String name) {
        for(Predefined predefined : Predefined.values()) {
            if (predefined.name().equalsIgnoreCase(name)) {
                return predefined.getService();
            }
        }
        return null;
    }
}
