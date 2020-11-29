package org.socyno.webfwk.module.release.change;

public interface AbsChangeDetail {
    
    Long getId();
    
    void setId(Long id);
    
    String getChangeType();
    
    void setChangeType(String changeType);
}
