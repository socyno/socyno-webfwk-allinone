package com.weimob.webfwk.module.application;

import java.util.List;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;

public class FieldApplicationAll extends FieldApplication {
    
    @Override
    public List<OptionApplication> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryWithAllOptions(OptionApplication.class, (FilterBasicKeyword)filter, false);
    }
    
}
