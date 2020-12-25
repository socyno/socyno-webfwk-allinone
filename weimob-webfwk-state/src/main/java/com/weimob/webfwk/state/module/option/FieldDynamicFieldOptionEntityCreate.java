package com.weimob.webfwk.state.module.option;

import com.weimob.webfwk.util.state.field.FieldTableView;

public class FieldDynamicFieldOptionEntityCreate extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass(){
        return DynamicFieldOptionEntity.class ;
    }
}
