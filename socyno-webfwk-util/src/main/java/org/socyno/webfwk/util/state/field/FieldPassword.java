package org.socyno.webfwk.util.state.field;

import com.github.reinert.jjschema.v1.FieldType;

public class FieldPassword extends FieldType {

    @Override
    public String getTypeName() {
        return "PASSWORD";
    }
}
