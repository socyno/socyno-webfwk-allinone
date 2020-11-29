package org.socyno.webfwk.module.dynamicoption;

import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldSystemFieldOptionEntityCreate extends FieldTableView {
    
    @Override
    public Class<?> getListItemCreationFormClass(){
        return SystemFieldOptionEntity.class ;
    }
}
