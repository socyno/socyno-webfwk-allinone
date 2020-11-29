package org.socyno.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.state.field.FieldSystemUsername;
import org.socyno.webfwk.state.field.OptionSystemUsername;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionSystemUsername extends Definition {
    
    @Getter
    private static final SuggerDefinitionSystemUsername instance = new SuggerDefinitionSystemUsername();
    private static final OptionClass<?> optionClass = new OptionClass<OptionSystemUsername>() {
        @Override
        protected Class<OptionSystemUsername> getType() {
            return null;
        }
        
        @Override
        public Class<?> getAttrType() {
            return FieldSystemUsername.class;
        }
        
        @Override
        protected Collection<OptionSystemUsername> queryOptions(Collection<OptionSystemUsername> values)
                throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionSystemUsername v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldSystemUsername.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionSystemUsername origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}