package com.weimob.webfwk.executor.api.chartmail;

import com.weimob.webfwk.executor.abs.AbstractJobExecutorService;
import com.weimob.webfwk.executor.abs.AbstractJobManager;

import lombok.Getter;

public class ChartMailService extends AbstractJobManager<ChartMailParams, ChartMailStatus> {
    
    public final static ChartMailService DEFAULT = new ChartMailService();
    
    @Getter
    public static enum Predefined {
        DEFAULT(ChartMailService.DEFAULT);
        
        private final ChartMailService service;
        
        Predefined(ChartMailService service) {
            this.service = service;
        }
    }
    
    @Override
    protected String getTaskPath() {
        return "chartMail";
    }
    
    @Override
    protected AbstractJobExecutorService getService() {
        return AbstractJobExecutorService.DEFAULT;
    }
    
    @Override
    protected int getTimeoutSeconds() {
        return 60000 * 30;
    }
    
    public static ChartMailService getInstance(String name) {
        for (Predefined predefined : Predefined.values()) {
            if (predefined.name().equalsIgnoreCase(name)) {
                return predefined.getService();
            }
        }
        return null;
    }
}
