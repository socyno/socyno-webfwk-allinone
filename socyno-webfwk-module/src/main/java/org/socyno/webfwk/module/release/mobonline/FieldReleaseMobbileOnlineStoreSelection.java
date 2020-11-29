package org.socyno.webfwk.module.release.mobonline;

import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldReleaseMobbileOnlineStoreSelection extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return OptionReleaseMobileOnlineAppStore.class;
    }
    
}
