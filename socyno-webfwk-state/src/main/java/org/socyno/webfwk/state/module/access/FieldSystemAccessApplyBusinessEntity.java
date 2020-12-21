package org.socyno.webfwk.state.module.access;

import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldSystemAccessApplyBusinessEntity extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return SystemAccessApplyBusinessEntity.class;
    }
}
