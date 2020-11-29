package org.socyno.webfwk.module.release.change;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

public class FieldChangeRequestReleaseIdMine extends FieldChangeRequestReleaseId {
    
    @Override
    public List<? extends OptionReleaseId> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryDynamicOptions(filter, true);
        
    }
    
}
