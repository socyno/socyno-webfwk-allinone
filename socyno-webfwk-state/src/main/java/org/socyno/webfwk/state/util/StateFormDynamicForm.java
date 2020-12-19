package org.socyno.webfwk.state.util;

import com.github.reinert.jjschema.SchemaIgnore;
import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StateFormDynamicForm extends StateFormBasicInput {
    @SchemaIgnore
    private JsonElement jsonData;
}
