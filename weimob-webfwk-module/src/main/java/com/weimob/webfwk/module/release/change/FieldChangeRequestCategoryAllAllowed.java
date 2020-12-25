package com.weimob.webfwk.module.release.change;

import java.util.List;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;

public class FieldChangeRequestCategoryAllAllowed extends FieldChangeRequestCategory {
    
    @Override
    public List<? extends FieldOption> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryDynamicOptions((FilterBasicKeyword)filter, true);
    }
}
