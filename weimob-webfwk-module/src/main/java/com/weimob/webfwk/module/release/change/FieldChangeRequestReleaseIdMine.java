package com.weimob.webfwk.module.release.change;

import java.util.List;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;

public class FieldChangeRequestReleaseIdMine extends FieldChangeRequestReleaseId {
    
    @Override
    public List<? extends OptionReleaseId> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryDynamicOptions((FilterBasicKeyword)filter, true);
    }
    
}
