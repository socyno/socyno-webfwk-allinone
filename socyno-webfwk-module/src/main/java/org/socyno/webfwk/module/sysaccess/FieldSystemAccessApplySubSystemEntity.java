package org.socyno.webfwk.module.sysaccess;

import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldSystemAccessApplySubSystemEntity extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return SystemAccessApplySubSystemEntity.class;
    }
}
