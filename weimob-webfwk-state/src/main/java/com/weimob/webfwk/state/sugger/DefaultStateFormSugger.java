package com.weimob.webfwk.state.sugger;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

public class DefaultStateFormSugger extends AbstractStateFormSugger {
    
    @Getter
    private static final DefaultStateFormSugger instance = new DefaultStateFormSugger();
    
    private static final List<Definition> DEFINITIONS = new LinkedList<Definition>() {
        private static final long serialVersionUID = 1L;
        {
            add(SuggerDefinitionDynamicOption.getInstance());
            add(SuggerDefinitionSystemUser.getInstance());
            add(SuggerDefinitionSystemUsername.getInstance());
            add(SuggerDefinitionSimpleAttachment.getInstance());
            add(SuggerDefinitionFormAttachment.getInstance());
            add(SuggerDefinitionSystemRole.getInstance());
            add(SuggerDefinitionSystemFeature.getInstance());
            add(SuggerDefinitionSystemBusiness.getInstance());
        }
    };
    
    public static void addFieldDefinitions(@NonNull Definition fieldDefinition) {
        DEFINITIONS.add(fieldDefinition);
        /**
         * optionClass 存在的将被优先使用
         */
        DEFINITIONS.sort(new Comparator<Definition>() {
            @Override
            public int compare(Definition l, Definition r) {
                return l.getOptionClass() == null ? 1 : -1;
            }
        });
    }
    
    @Override
    protected Collection<Definition> getFieldDefinitions() {
        return DEFINITIONS;
    }
}
