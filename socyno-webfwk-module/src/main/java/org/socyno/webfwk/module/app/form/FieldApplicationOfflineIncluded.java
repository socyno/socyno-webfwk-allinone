package org.socyno.webfwk.module.app.form;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;


public class FieldApplicationOfflineIncluded extends FieldApplication {
    @Override
    public List<OptionApplication> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryOnlyVisibleOptions(OptionApplication.class, filter, true);
    }
}
