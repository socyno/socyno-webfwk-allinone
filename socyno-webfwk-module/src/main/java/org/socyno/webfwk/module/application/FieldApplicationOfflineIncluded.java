package org.socyno.webfwk.module.application;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;


public class FieldApplicationOfflineIncluded extends FieldApplication {
    @Override
    public List<OptionApplication> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryOnlyVisibleOptions(OptionApplication.class, (FilterBasicKeyword)filter, true);
    }
}
