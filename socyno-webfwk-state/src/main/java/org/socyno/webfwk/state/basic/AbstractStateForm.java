package org.socyno.webfwk.state.basic;

public interface AbstractStateForm {
    
    public Long getId();
    public void setId(Long formId);
    public Long getRevision();
    public void setRevision(Long revision);
    public String getState();
    public void setState(String state);
}
