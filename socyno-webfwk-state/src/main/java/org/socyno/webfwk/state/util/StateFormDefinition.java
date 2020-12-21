package org.socyno.webfwk.state.util;

import java.util.List;
import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class StateFormDefinition {
    private String name;
    private String title;
    private String formClass;
    private boolean commentSupported;
    private String[] allownActions;
    private String properties;
    private List<? extends FieldOption>    states;
    private List<StateFormQueryDefinition>  queries;
    private List<StateFormActionDefinition> actions;
}
