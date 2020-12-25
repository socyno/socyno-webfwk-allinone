package com.weimob.webfwk.state.module.todo;

import java.util.List;

import com.weimob.webfwk.state.field.OptionSystemUser;

public interface SystemTodoFormWithAssignees {
    
   public List<OptionSystemUser> getAssignees() ;
   
  public void setAssignees(List<OptionSystemUser> assignees) ;
    
}
