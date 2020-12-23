package org.socyno.webfwk.state.module.todo;

import java.util.List;

import org.socyno.webfwk.state.field.OptionSystemUser;

public interface SystemTodoFormWithAssignees {
    
   public List<OptionSystemUser> getAssignees() ;
   
  public void setAssignees(List<OptionSystemUser> assignees) ;
    
}
