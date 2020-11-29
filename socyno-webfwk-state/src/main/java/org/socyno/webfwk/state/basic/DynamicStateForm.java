package org.socyno.webfwk.state.basic;

import com.github.reinert.jjschema.SchemaIgnore;
import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DynamicStateForm extends BasicStateForm {
    @SchemaIgnore
    private JsonElement jsonData;
}
