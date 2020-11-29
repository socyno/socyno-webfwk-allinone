package org.socyno.webfwk.module.release.change;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

import com.github.reinert.jjschema.v1.FieldOption;

public class FieldChangeRequestCategoryAllAllowed extends FieldChangeRequestCategory {
    
    @Override
    public List<? extends FieldOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryDynamicOptions(filter, true);
    }
}
