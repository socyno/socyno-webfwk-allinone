package org.socyno.webfwk.module.release.change;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;

public class FieldChangeRequestReleaseIdMine extends FieldChangeRequestReleaseId {
    
    @Override
    public List<? extends OptionReleaseId> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryDynamicOptions((FilterBasicKeyword)filter, true);
    }
    
}
