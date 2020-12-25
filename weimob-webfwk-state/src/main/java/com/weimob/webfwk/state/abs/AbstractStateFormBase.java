package com.weimob.webfwk.state.abs;

import java.util.Date;

public interface AbstractStateFormBase extends AbstractStateFormInput {
    
    public String getState();
    
    public void setState(String state);
    
    public Date getCreatedAt();
    
    public void setCreatedAt(Date createdAt);
    
    public Long getCreatedBy();
    
    public void setCreatedBy(Long createdBy);
    
    public String getCreatedCodeBy();
    
    public void setCreatedCodeBy(String createdCodeBy);
    
    public String getCreatedNameBy();
    
    public void setCreatedNameBy(String createdNameBy);
    
    public Date getUpdatedAt();
    
    public void setUpdatedAt(Date updatedAt);
    
    public Long getUpdatedBy();
    
    public void setUpdatedBy(Long updatedBy);
    
    public String getUpdatedCodeBy();
    
    public void setUpdatedCodeBy(String updatedCodeBy);
    
    public String getUpdatedNameBy();
    
    public void setUpdatedNameBy(String updatedNameBy);
}
