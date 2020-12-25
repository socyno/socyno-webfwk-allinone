package com.weimob.webfwk.module.release.mobonline;

import com.weimob.webfwk.util.state.field.FieldTableView;

public class FieldReleaseMobbileOnlineStoreSelection extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return OptionReleaseMobileOnlineAppStore.class;
    }
    
}
