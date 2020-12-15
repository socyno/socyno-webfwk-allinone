package org.socyno.webfwk.module.release.change;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.module.release.change.FieldChangeRequestReleaseId.OptionReleaseId;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionChangeReleaseId extends Definition {
    
    @Getter
    private static final SuggerDefinitionChangeReleaseId Instance = new SuggerDefinitionChangeReleaseId();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionReleaseId>() {
        @Override
        protected Class<OptionReleaseId> getType() {
            return OptionReleaseId.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Collection<OptionReleaseId> queryOptions(Collection<OptionReleaseId> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionReleaseId v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return (Collection<OptionReleaseId>) FieldChangeRequestReleaseId
                    .queryDynamicValues(optionValues.toArray(new String[0]));
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionReleaseId origin, OptionWrapper wrapper,
                Field field, Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
