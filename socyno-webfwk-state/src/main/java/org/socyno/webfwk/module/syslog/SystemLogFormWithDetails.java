package org.socyno.webfwk.module.syslog;

public interface SystemLogFormWithDetails {
    
    public Long getOperateDetailId();
    
    public String getOperateBefore();
    
    public String getOperateAfter();
    
    public void setOperateBefore(String data);
    
    public void setOperateAfter(String data);
    
}
