package org.socyno.webfwk.module.vcs.change;

import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;

public interface VcsChangeInfoWithApplication extends VcsChangeInfoAbstractForm {
    
    public OptionApplication getApplication();
    
    public void setApplication(OptionApplication application);
}
