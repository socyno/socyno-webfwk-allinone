package org.socyno.webfwk.state.field;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;

public interface FilterAbstractFrom extends FieldOptionsFilter {
    public String getFormName();
    public void setFormName(String formName);
}
