package com.weimob.webfwk.state.module.access;

import com.weimob.webfwk.util.state.field.FieldTableView;

public class FieldSystemAccessApplyBusinessEntity extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return SystemAccessApplyBusinessEntity.class;
    }
}
